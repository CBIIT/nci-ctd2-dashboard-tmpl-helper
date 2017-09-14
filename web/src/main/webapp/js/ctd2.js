!function ($) {
    // This is strictly coupled to the homepage design!
    var numOfStoriesHomePage = 4;
    var numOfCartGene = 25    

    // These seperators are for replacing items within the observation summary
    var leftSep = "<";
    var rightSep = ">";

    // To make URL constructing more configurable
    var CORE_API_URL = "./";

    // This is for the moustache-like templates
    // prevents collisions with JSP tags <%...%>
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };

    // Get these options from the page
    var maxNumberOfEntities = $("#maxNumberOfEntites").html() * 1;

    // Datatables fix
    $.extend($.fn.dataTableExt.oStdClasses, {
        "sWrapper": "dataTables_wrapper form-inline"
    });

    $.extend( true, $.fn.dataTable.defaults, {
        "oLanguage": { // "search" -> "filter"
            "sSearch": "Filter Table:"
        },
        "search": { // simple searching
            "smart": false
        },
        // These are for bootstrap-styled datatables
        "sDom": "<'row'<'span6'i><'span6'f>r>t<'row'<'span6'l><'span6'p>>",
        "sPaginationType": "bootstrap"
    });

    // Let datatables know about our date format
    $.extend($.fn.dataTable.ext.order, {
        "dashboard-date": function(settings, col) {
            return this.api().column( col, {order:'index'} ).nodes().map(
                function(td, i) {
                    return (new Date($('a', td).html())).getTime();
                }
            );
        }
    });

    // Let datatables know about dashboard rank (for sorting)
    $.extend($.fn.dataTable.ext.order, {
        "dashboard-rank": function(settings, col) {
            return this.api().column( col, {order:'index'} ).nodes().map(
                function(td, i) {
                    return $('ul', td).attr("data-score");
                }
            );
        }
    });

    // Let datatables know about observation count (for sorting)
    $.extend($.fn.dataTable.ext.type.order, {
        "observation-count-pre": function(d) {
            if(d==null || d=="") return 0;
            var start = d.indexOf(">");
            var end = d.indexOf("<", start);
            if(end<=start) return 0;
            var count_text = d.substring(start+1, end);
            var count = 0;
            if(count_text != undefined) count = parseInt(count_text);
            return count;
        }
    });

    $.fn.dataTable.Api.register( 'order.neutral()', function () {
    	return this.iterator( 'table', function ( s ) {
    		s.aaSorting.length = 0;
    		s.aiDisplay.sort( function (a,b) {
    			return a-b;
    		} );
    		s.aiDisplayMaster.sort( function (a,b) {
    			return a-b;
    		} );
    	} );
    } );

    /* Models */
    var SubmissionCenter = Backbone.Model.extend({
        urlRoot: CORE_API_URL + "get/center"
    });
    var SubmissionCenters = Backbone.Collection.extend({
        url: CORE_API_URL + "list/center/?filterBy=",
        model: SubmissionCenter
    });

    /* Views */
    var HelpNavigateView = Backbone.View.extend({
        template: _.template($("#help-navigate-tmpl").html()),

        render: function() {
            var content = this.template({});

            $.fancybox(
                content,
                {
                    'autoDimensions' : false,
                    'width': '75%',
                    'height': '99%',
                    'centerOnScroll': true,
                    'transitionIn' : 'none',
                    'transitionOut' : 'none'
                }
            );

            return this;
        }
    });

    //Gene List View
    var GeneListView = Backbone.View.extend({
    	el: $("#main-container"),
        template: _.template($("#genelist-view-tmpl").html()),      
        render: function() { 
        	
        	var geneList = JSON.parse(localStorage.getItem("genelist")); 
        	 if (geneList == null)                       
        		 geneList = [];
        	 else if (geneList.length > numOfCartGene)
        	 {
        		 var len = geneList.length
        		 geneList.slice(numOfCartGene, len-1);
        		 localStorage["genelist"] = JSON.stringify(geneList);
        	 }
                 
        	var html = "";
        	$(this.el).html(this.template({}));   
        	$.each(geneList, function (aData) {
        		var value = Encoder.htmlEncode(this.toString());                   	    
        	    $("#geneNames").append(_.template($("#gene-cart-option-tmpl").html(), {displayItem: value})); 
            });        	
        	 
            $("#addGene").click(function(e) {        		 
       		   e.preventDefault();        		 
       		    
       		   $("#gene-symbols").val("");
               $("#addgene-modal").modal('show');
             
            });  
            
            $("#add-gene-symbols").click(function() {
            	var inputGenes = $("#gene-symbols").val();                
            	var genes = Encoder.htmlEncode(inputGenes).split(/[\s,]+/);            	 
            	
        		processInputGenes(genes);              
        		
            });
            
     
            $("#deleteGene").click(function(e) {        		 
       		    e.preventDefault(); 
       		    var selectedGenes = [];       		    
       		    $('#geneNames :selected').each(function(i, selected) {
       			     selectedGenes[i] = $(selected).text();
       		    });
       		 
       		    if (selectedGenes == null || selectedGenes.length == 0)
       		    {
       		    	showAlertMessage("You haven't select any gene!");
       		   	   return;
       		    }      		 
       		   
       		  
       		    $.each(selectedGenes, function () {    
       		    	 
      		       var gene = $.trim(this.toString()).toUpperCase();      		 
      		       var index = $.inArray(gene, geneList);      		  
      		       if (index>=0) geneList.splice(index, 1);     
      		        
                });   
       		    localStorage["genelist"] = JSON.stringify(geneList);
       		    sessionStorage["selectedGenes"] = JSON.stringify(geneList);
       		    $("#geneNames option:selected").remove();  
       		  
       		
             });  
       		 
             
            $("#clearList").click(function(e) {        		 
       		    e.preventDefault();        		    
       		    $('#geneNames').html('');       		     
       		    localStorage.removeItem("genelist");
       		    sessionStorage.removeItem("selectedGenes");
       		    
       		    geneList = [];
       		    
       		    
            });  
            
            $("#loadGenes").click(function(e) {        		 
       		    e.preventDefault();        		    
       		    $('#geneFileInput').click();       		  
       		   
             });
            
            if (window.FileReader) {
                 $('#geneFileInput').on('change', function (e) {                    
                     var file = e.target.files[0];                    
                     if (file.size > 1000)
                     {                    	 
              	    	showAlertMessage("Gene Cart can only contains " + numOfCartGene + " genes.");
                        return;
                     }
                     var reader = new FileReader();                   
                     reader.onload = function (e) {
                         var genes = reader.result.split(/[\s,]+/);  
                        
                         processInputGenes(genes);
                     }
                     reader.readAsText(file);
                     $('#geneFileInput').each(function() {
                         $(this).after($(this).clone(true)).remove();
                        });
                 });
            } else {             
     	    	showAlertMessage("Load Genes from file is not supported.");
            }            
            
            $("#cnkb-query").click(function(e) {        		 
       		   
       		   var selectedGenes = [];       		    
       		   $('#geneNames :selected').each(function(i, selected) {
       			     selectedGenes[i] = $(selected).text();
       		   });
       		 
       		    if (selectedGenes == null || selectedGenes.length == 0)
       		    {
       		    	sessionStorage["selectedGenes"] = JSON.stringify(geneList); 
       		     
       		    }      		 
       		    else
       		    {       		       
       		    	sessionStorage["selectedGenes"] = JSON.stringify(selectedGenes);       		     
       		    } 
       		    
             });     
            
            
            var processInputGenes = function(genes)
            {
            	var geneNames = JSON.parse(localStorage.getItem("genelist"));
                if (geneNames == null)                       
              	   geneNames = [];
                var num = genes.length + geneNames.length                        
                if ( num > numOfCartGene)
                {                	 
         	    	showAlertMessage("Gene Cart can only contains " + numOfCartGene + " genes.");
                    return;
                }
                 
            	 
            	$.ajax({
                    url: "cnkb/validation",
                    data: {  
                    	     geneSymbols: JSON.stringify(genes) 
                 	      },
                    dataType: "json",
                    contentType: "json",                   
                    success: function(data) {                    	 
                    	var invalidGenes = "";
                        _.each(data, function(aData){      
                        	 if ( invalidGenes.length > 0)
                        	    invalidGenes = aData;
                        	 else
                        		invalidGenes = invalidGenes + ", " + aData; 
                        	 genes.splice(genes.indexOf(aData),1)
                        });   
                 	    if (data.length > 1)
                    	{ 
                 	    	showInvalidMessage("\"" + data + "\" are invalid and not added to the cart.")
                 	    }
                 	        
                    	else if (data.length == 1)
                    	{                  		 
                    		showInvalidMessage("\"" + data + "\" is invalid and not added to the cart.")
                    	}
                    	else 
                    	{                    		 
                    		$("#addgene-modal").modal('hide');
                    		 
                    	}
                 	    
                 	    addGenes(genes);
                 	    
                    }
                 });  //ajax   
            }
            
            
            var addGenes = function(genes)
            {
                  var alreadyHave = [];
                  var newGenes = [];  
            	  $.each(genes, function () {
                 	 var eachGene = Encoder.htmlEncode($.trim(this.toString())).toUpperCase();
                 	 if (geneList.indexOf(eachGene) > -1)
                 		 alreadyHave.push(eachGene);
                 	 else if (newGenes.indexOf(eachGene.toUpperCase()) == -1 && eachGene != "") 
                 	 {                       		
                 		 newGenes.push(eachGene);  
                 		 geneList.push(eachGene);
                 	 }
                  });
            	  
         		  if (newGenes.length > 0)
 		          {        			 
 			           localStorage["genelist"] = JSON.stringify(geneList);        		             
 		               $.each(newGenes, function () {
     		                 var value = this.toString();    		                     
     		                 $("#geneNames").append(_.template($("#gene-cart-option-tmpl").html(), {displayItem: value})); 
                        });        	
     	                
 		           }
            }
            
            
        	return this;
        }
        
	 
    });
    
    
    
    var CnkbQueryView = Backbone.View.extend({
    	el: $("#main-container"),
        template: _.template($("#cnkb-query-tmpl").html()),      
        render: function() {        	 
        	var selectedGenes = JSON.parse(sessionStorage.getItem("selectedGenes"));    
        	var count = 0;
        	if (selectedGenes != null)
        		count = selectedGenes.length;        	
        	var description;
        	if (count == 0 || count == 1)
        		description = "Query with " + count + " gene from cart";        	
        	else
        		description = "Query with " + count + " genes from cart";  
        	
        	$(this.el).html(this.template({}));   
        	$('#queryDescription').html("");                     
            $('#queryDescription').html(description);
        	$.ajax({
                   url: "cnkb/query",
                   data: {dataType : "interactome-context", 
                	      interactome: "", 
                	      version: "", 
                	      selectedGenes: "", 
                	      interactionLimit: 0, 
                	      throttle : ""},
                   dataType: "json",
                   contentType: "json",                   
                   success: function(data) {
                       var list = data.interactomeList;
                       _.each(list, function(aData){
                           if(aData.toLowerCase().startsWith("preppi")) {
                               $("#interactomeList").prepend(_.template($("#gene-cart-option-tmpl-preselected").html(), {displayItem: aData}));
                               var interactome = aData.split("(")[0].trim();
                               $.ajax({ // query the description
                                   url: "cnkb/query",
                                   data: {dataType : "interactome-version", interactome: interactome, version: "", selectedGenes: "", interactionLimit: 0, throttle: ""},
                                   dataType: "json",
                                   contentType: "json",
                                   success: function(data) {
                                       $('#interactomeDescription').html("");
                                       $('#interactomeDescription').html(convertUrl(data.description));
                                       $('#interactomeVersionList').html("");
                                       _.each(data.versionDescriptorList, function(aData){
                                              $("#interactomeVersionList").append(_.template($("#gene-cart-option-tmpl").html(), {displayItem: aData.version})); 
                                       }); 
                                       $('#interactomeVersionList').disabled = false;
                                       $('#selectVersion').css('color', '#5a5a5a');
                                       $('#versionDescription').html("");
                                   }
                               });  //ajax
                           } else
                               $("#interactomeList").append(_.template($("#gene-cart-option-tmpl").html(), {displayItem: aData}));
                       });
                       $('#interactomeVersionList').disabled = true;
                  }
            });  //ajax   
        	
        	var versionDescriptors;
        	$('#interactomeList').change(function(){         		
        		var selectedInteractome = $('#interactomeList option:selected').text().split("(")[0].trim();            		 
        	    $.ajax({
                    url: "cnkb/query",
                    data: {dataType : "interactome-version", interactome: selectedInteractome, version: "", selectedGenes: "", interactionLimit: 0, throttle: ""},
                    dataType: "json",
                    contentType: "json",                   
                    success: function(data) {  
                    	versionDescriptors = data.versionDescriptorList;
                        var description = data.description;                      
                        $('#interactomeDescription').html("");                     
                        $('#interactomeDescription').html(convertUrl(description));                        
                 	    var list = data.versionDescriptorList; 
                 	   $('#interactomeVersionList').html("");                 	 
                        _.each(list, function(aData){               		        
                		       $("#interactomeVersionList").append(_.template($("#gene-cart-option-tmpl").html(), {displayItem: aData.version})); 
                        }); 
                        $('#interactomeVersionList').disabled = false;
                        $('#selectVersion').css('color', '#5a5a5a');
                        $('#versionDescription').html("");  
                       
                     }
                 });  //ajax        	    
        	  
        	 });  //end $('#interactomeList').change()
            
        	 $('#interactomeVersionList').change(function(){         		  
        		   var selectedVersion = $('#interactomeVersionList option:selected').text().trim();        		  
        	       _.each(versionDescriptors, function(aData){         	    	 
                		if (aData.version === selectedVersion)
                		{               		 
                			$('#versionDescription').html("");                     
                            $('#versionDescription').html(aData.versionDesc);
                		}	
                   }); 
                         
                       	    
        	  
        	  });  //end $('#interactomeList').change()
        	
        	  $("#cnkb-result").click(function(e) {        		 
         		   
        		   var selectedInteractome = $('#interactomeList option:selected').text().split("(")[0].trim(); 
                   var selectedVersion = $('#interactomeVersionList option:selected').text().trim(); 
                   
                   if (selectedInteractome == null || $.trim(selectedInteractome).length == 0)
                   {
                	   e.preventDefault(); 
                	   showAlertMessage("Please select an interactome name");
                	   
                   } else if (selectedVersion == null || $.trim(selectedVersion).length == 0)
                   {
                	   e.preventDefault(); 
                	   showAlertMessage("Please select an interactome version.");
                   }
                   else
                   {
        		       sessionStorage["selectedInteractome"] = JSON.stringify(selectedInteractome);        		    
        		       sessionStorage["selectedVersion"] = JSON.stringify(selectedVersion);
                   }
        		
               });  
            
        	return this;
        }
        
	 
    });

    var CnkbResultView = Backbone.View.extend({
    	el: $("#main-container"),
        template: _.template($("#cnkb-result-tmpl").html()),      
        render: function() { 
        	var selectedgenes = JSON.parse(sessionStorage.getItem("selectedGenes"));        
        	var selectedInteractome = JSON.parse(sessionStorage.getItem("selectedInteractome")); 
        	var selectedVersion = JSON.parse(sessionStorage.getItem("selectedVersion"));  
        	
        	if (selectedgenes.length > numOfCartGene)
       	    {
       		    var len = selectedgenes.length
       		    selectedgenes.slice(numOfCartGene, len-1);
       		    sessionStorage["selectedGenes"] = JSON.stringify(selectedgenes);
       	    }
        	
        	$(this.el).html(this.template({}));        	
        	$.ajax({       		 
        		   url: "cnkb/query",
                   data: {dataType : "interaction-result", 
                	      interactome: selectedInteractome, 
                	      version: selectedVersion, 
                	      selectedGenes: JSON.stringify(selectedgenes), 
                	      interactionLimit: 0, 
                	      throttle: ""},
                   dataType: "json",
                   contentType: "json",                                
                   success: function(data) {    
                	   $("#cnkb_data_progress").hide();
                	   var cnkbElementList = data.cnkbElementList; 
                	   var interactionTypes = data.interactionTypeList;               	   
                       _.each(interactionTypes, function(aData){  
                           var type = aData.toUpperCase();
               		       $('#cnkb-result-grid thead tr').append('<th>' +type + '</th>');                          
                       });  
                       
                       var thatEl = $("#cnkb-result-grid");   
                	   _.each(cnkbElementList, function(aData){                		  
                		   var cnkbResultRowView = new CnkbResultRowView({
                                el: $(thatEl).find("tbody"),
                                model: aData                               
                            });
                		   cnkbResultRowView.render();                		    
                          
                       });   
                	   
                	   var oTable1 = $('#cnkb-result-grid').dataTable({
                           "sDom": "<'row'<'span5'i><'span5'f>r>t<'row'<'span5'l><'span5'p>>",
                           "sScrollY": "200px",
                            "bPaginate": false
                          
                   	   });
                   
                  }
                  
            });  //ajax  
        	
        	
        	$('#cnkbExport').click(function(e) {         		
       		    e.preventDefault();     
       	        var filters = "";
                $('input[type="checkbox"]:checked').each(function() {                 
                	filters = filters + ($(this).val() + ',');    
                });  
                if (filters.length == 0 || $.trim(filters) === 'on,') {
              	  showAlertMessage("Please select at least one row to export to a SIF file.");
                     return;
                }
                
                $("#interactome").val(selectedInteractome);
                $("#version").val(selectedVersion);
                $("#selectedGenes").val(filters);                 
                $("#interactionLimit").val("0");
                $("#throttle").val("");
                $('#cnkbExport-form').submit() 
        	  
        	 });  //end $('#interactomeList').change()
        	
        	 var getThrottleValue = function() {
        			 
        		     var interactionLimit = $("#cytoscape-node-limit").val();
        		     var filters = "";
        	        $('input[type="checkbox"]:checked').each(function() {                 
        	        	filters = filters + ($(this).val() + ',');             	    
        	        });             
        	    
        	        $.ajax({
        	     	    url: "cnkb/query",
        	           data: {dataType : "interaction-throttle",  
        	       	       interactome: selectedInteractome, 
        	   	           version: selectedVersion, 
        	   	           selectedGenes: filters,
        	   	           interactionLimit: interactionLimit, 
        	   	           throttle : ""},
        	           dataType: "json",
        	           contentType: "json",
        	           success: function(data) {                     	 
        	              if (data != null && data.threshold != -1)   
        	              {
        	           	   if (data.threshold == 0)
        	                     $("#throttle-input").text("0.0"); 
        	           	   else
        	           		  $("#throttle-input").text(data.threshold);
        	              } 
        	              else
        	                 $("#throttle-input").text("e.g. 0.01");
        	              $("#throttle-input").css('color', 'grey');                          
        	           }
        	       });
        	   	
        	   };
        	
        	 $("#cnkb-result-grid").on("change", ":checkbox", function() {        		 
        		 getThrottleValue();  
             });  //end cnkb-checked         	    
        	 
             $("#cytoscape-node-limit").change(function(evt) {             
            	 getThrottleValue();                 
             }); 
            
              
             $('#checkbox_selectall').click(function(event) {  //on click
                   if(this.checked) { // check select status
                        $('.cnkb_checkbox').each(function() { //loop through each checkbox
                            this.checked = true;  //select all checkboxes with class "checkbox1"              
            	        });
                        getThrottleValue();  
                    }else{
                        $('.cnkb_checkbox').each(function() { //loop through each checkbox
                            this.checked = false; //deselect all checkboxes with class "checkbox1"                      
                        });   
                        $("#throttle-input").text("e.g. 0.01");
                        $("#throttle-input").css('color', 'grey');   
                    }
             });  
             

             $('#createnetwork').click(function(event) {            	
                      event.preventDefault();                   
                      var throttle = $("#throttle-input").text();               
                      var layoutName = $("#cytoscape-layouts").val();
                      var interactionLimit = $("#cytoscape-node-limit").val();
                    
                      var n = $( "input:checked" ).length;
                   
                      var filters = "";              
                      $('input[type="checkbox"]:checked').each(function() {                 
                      	    filters = filters + ($(this).val() + ',');  
                      	  
                      });   
                      
                    
                      if (filters.length == 0 || $.trim(filters) === 'on,') {
                    	  showAlertMessage("Please select at least one row to create a network.");
                           return;
                      }
                      $('#createnw_progress_indicator').show();               
                      $.ajax({
                      	 url: "cnkb/network",
                          data: { interactome: selectedInteractome, 
           	   	                 version: selectedVersion, 
         	   	                 selectedGenes: filters,
         	   	                 interactionLimit: interactionLimit, 
         	   	                 throttle : throttle },
                          dataType: "json",
                          contentType: "json",
                          success: function(data) {   
                              $('#createnw_progress_indicator').hide();
                          	  if (data == null)
                              {
                          		showAlertMessage("The network is empty.");
                          		 return;
                              }                          	  
                          	  var cnkbDescription = selectedInteractome + " (v" + selectedVersion + ")";
                              drawCNKBCytoscape(data, Encoder.htmlEncode(cnkbDescription));
                        
                          }//end success
                      });  //end ajax              
                    

                  });  //end createnetwork              
             
        	return this;
        }       
	 
     });    
   
    
     var CnkbResultRowView = Backbone.View.extend({
        render: function() {
            var result = this.model;
            
            var templateId = "#cnkb-result-row-tmpl";     

            this.template = _.template($(templateId).html());
            $(this.el).append(this.template(result));            
            var geneName = Encoder.htmlEncode(result.geneName);
 	      
		    var numList = result.interactionNumlist
		       _.each(numList, function(aData){ 
		    	   $("#tr_" + geneName).append('<td>' + aData + '</td>');
		    });
		  
		       
            return this;
        }
     });    
     
     var GeneCartHelpView = Backbone.View.extend({
    	 el: $("#main-container"),
         template: _.template($("#gene-cart-help-tmpl").html()),      
         render: function() {        	 
             $(this.el).html(this.template({}));               
             return this;
         }
      });    
     
     
     var updateGeneList = function(addedGene)
     {
    	   var geneNames = JSON.parse(localStorage.getItem("genelist"));
           if (geneNames == null)                       
        	   geneNames = [];
                    
           if (geneNames.length >= numOfCartGene)
           {
        	   showAlertMessage("Gene Cart can only contains " + numOfCartGene + " genes.")
        	   return;
           }
           
           if (geneNames.indexOf(addedGene) > -1) {       	   
        	   showAlertMessage(addedGene + " is already in the Gene Cart.")        	  
        	} else {
        	    //Not in the array            	
        		geneNames.push(addedGene);
        		localStorage["genelist"] = JSON.stringify(geneNames);        		
        		showAlertMessage(addedGene + " added to the Gene Cart.")
        	}
       }   
     
     
       var showAlertMessage = function(message)
       {    	 
    	  $("#alertMessage").text(message);
    	  $("#alertMessage").css('color', '#5a5a5a');   
    	  $("#alert-message-modal").modal('show');
       }   
     
       var showInvalidMessage = function(message)
       {    	 
    	  $("#alertMessage").text(message);
    	  $("#alertMessage").css('color', 'red');
    	  $("#alert-message-modal").modal('show');
       }   
       
       var convertUrl = function(description)
       {
    	   if ( description.indexOf("http:") > -1)
    	   {    		  	       
    	       var word=description.split("http:");
    	       var temp = $.trim(word[1]);
    	       if ( temp.match(/.$/))
    	    	   temp = temp.substring(0, temp.length-1);
    	       temp = $.trim(temp);
    	       var link = "<a target=\"_blank\" href=\"" + temp +"\">" + temp + "</a>";  
    	       return word[0] + link;     	        
    	   }
    	   else    	   
    		   return description;
    	    
       }
       
       var drawCNKBCytoscape = function(data, description)
       {    	     
    		  var svgHtml = "";
        	  var interactions = data.interactions; 
        	  var x1 =20+90*(3-interactions.length),  x2=53+90*(3-interactions.length);
            _.each(interactions, function(aData){                  	                           	 
            	svgHtml = svgHtml + '<rect x="' + x1 + '" y="15" width="30" height="2" fill="' + aData.color +'" stroke="grey" stroke-width="0"/><text x="' + x2 + '" y="20" fill="grey">' + aData.type + '</text>';    
                x1 = x1 + aData.type.length * 11;
                x2 = x2 + aData.type.length * 11;
            });  
        
            $.fancybox(
                _.template($("#cnkb-cytoscape-tmpl").html(), { description: description, svgHtml: svgHtml }),
                {
                    'autoDimensions' : false,
                    'width' : '100%',
                    'height' : '85%',
                    'transitionIn' : 'none',
                    'transitionOut' : 'none'
                }
            );                   
         
            var container = $('#cytoscape');                        
            var layoutName = $("#cytoscape-layouts").val();
             
            var cy = cytoscape ({
            	
            	container: document.getElementById("cytoscape"),
                layout: {
                	 name: layoutName,
                	 fit: true,                                                  	 
                	 liveUpdate: false,                       
                	 maxSimulationTime: 4000, // max length in ms to run the layout                        
                	 stop: function(){
                		 $("#cnkb_cytoscape_progress").remove();
                		 this.stop();                		
                		 
                	 } // callback on layoutstop 
                	
                },
                elements: data,
                style: cytoscape.stylesheet()
                    .selector("node")
                    .css({
                        "content": "data(id)",                                              
                        "border-width": 2,
                        "labelValign": "middle",
                        "font-size": 10,                                                                  
                        "width": "25px",
                        "height": "25px",                                   
                        "background-color": "data(color)",
                        "border-color": "#555"
                    })
                    .selector("edge")
                    .css({
                        "width": "mapData(weight, 0, 100, 1, 3)",
                        "target-arrow-shape": "circle",
                        "source-arrow-shape": "circle",
                        "line-color": "data(color)"
                    })
                    .selector(":selected")
                    .css({
                        "background-color": "#000",
                        "line-color": "#000",
                        "source-arrow-color": "#000",
                        "target-arrow-color": "#000"
                    })
                    .selector(".ui-cytoscape-edgehandles-source")
                    .css({
                        "border-color": "#5CC2ED",
                        "border-width": 2
                    })
                    .selector(".ui-cytoscape-edgehandles-target, node.ui-cytoscape-edgehandles-preview")
                    .css({
                        "background-color": "#5CC2ED"
                    })
                    .selector("edge.ui-cytoscape-edgehandles-preview")
                    .css({
                        "line-color": "#5CC2ED"
                    })
                    .selector("node.ui-cytoscape-edgehandles-preview, node.intermediate")
                    .css({
                        "shape": "rectangle",
                        "width": 15,
                        "height": 15
                    })
                ,

                ready: function(){                    	 
                    window.cy = this; // for debugging             
                  
                }
            });   
            
            cy.on('cxttap', 'node', function(){
      		 
      			  $.contextMenu( 'destroy', '#cytoscape' );
      			  var sym = this.data('id');
      			  $.contextMenu({
                      selector: '#cytoscape', 
                     
                      callback: function(key, options) {
                                  var m = "clicked: " + key + " on " + sym;
                                  if(!key || 0 === key.length)
                                  {
                                	  $.contextMenu( 'destroy', '#cytoscape' );
                                	  return;
                                  }	
                                  
                                  var linkUrl = "";                                 
                                  switch(key) {
                                      case 'gene': 
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/gene?cmd=Search&term="+sym;                                    	  
                                    	  break;
                                      case 'protein':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/protein?cmd=Search&term=" + sym + "&doptcmdl=GenPept";                                    	  
                                    	  break;
                                      case 'pubmed':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/pubmed?cmd=Search&term=" + sym + "&doptcmdl=Abstract";                                    	  
                                    	  break;
                                      case 'nucleotide':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/nucleotide?cmd=Search&term=" + sym + "&doptcmdl=GenBank";                                    	  
                                    	  break;
                                      case 'alldatabases':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/gquery/?term="+sym;                                    	  
                                    	  break;
                                      case 'structure':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/structure?cmd=Search&term=" + sym + "&doptcmdl=Brief";                                    	  
                                    	  break;
                                      case 'omim':
                                    	  linkUrl = "http://www.ncbi.nlm.nih.gov/omim?cmd=Search&term=" + sym + "&doptcmdl=Synopsis";                                    	  
                                    	  break;
                                      case 'genecards':
                                    	  linkUrl = "http://www.genecards.org/cgi-bin/carddisp.pl?gene=" + sym + "&alias=yes";                                    	  
                                    	  break;
                                      case 'ctd2-dashboard' : 
                                    	  linkUrl = CORE_API_URL + "#search/" + sym;
                                  
                                  }
                                 // alert("test3");
                                  window.open(linkUrl);
                                  $.contextMenu( 'destroy', '#cytoscape' );
                              },
                      items: {
                                  "linkout": {"name": 'LinkOut'},
                                  "sep1": "---------",
                                  "entrez": {
                                              "name": "Entrez", 
                                              "items": {
                                                  "gene": {"name": "Gene"},
                                                  "protein": {"name": "Protein"},
                                                  "pubmed": {"name": "PubMed"},
                                                  "nucleotide": {"name": "Nucleotide"},
                                                  "alldatabases": {"name": "All Databases"},
                                                  "structure": {"name": "Structure"},
                                                  "omim": {"name": "OMIM"}
                                              }
                                          },
                                   "genecards": {"name": "GeneCards"},         
                                   "ctd2-dashboard": {"name": "CTD2-Dashboard"}   
                      
                      }
                                
                  });  
      			  
      			 
      		}); 
            
           
     }   
    

    /* Routers */
    AppRouter = Backbone.Router.extend({
        routes: {
            "template-helper": "showTemplateHelper",
            "about": "helpNavigate",
            "genes": "showGenes",
            "cnkb-query": "showCnkbQuery",
            "cnkb-result": "showCnkbResult", 
            "gene-cart-help": "showGeneCartHelp",
            "help-navigate": "helpNavigate",
            "*actions": "showTemplateHelper"
        },

        helpNavigate: function() {
            var helpNavigateView = new HelpNavigateView();
            helpNavigateView.render();
        },

        showGenes: function() {
            var geneListView = new GeneListView();
            geneListView.render();
        },
        
        showCnkbQuery: function() {
            var cnkbQueryView = new CnkbQueryView();
            cnkbQueryView.render();
        },
        
        showCnkbResult: function() {
        	var cnkbResultView = new CnkbResultView();
        	cnkbResultView.render();
        },        
       
        showGeneCartHelp: function() {
        	var geneCartHelpView = new GeneCartHelpView();
        	geneCartHelpView.render();
        },

        showTemplateHelper: function() {
            $ctd2.SubmissionCenters = SubmissionCenters;
            var templateHelperView = new $ctd2.TemplateHelperView();
            templateHelperView.render();
        }
    });

    $(function(){
        new AppRouter();
        Backbone.history.start();

        $("#omnisearch").submit(function() {
            var searchTerm = $("#omni-input").val();
            window.location = "/dashboard/#search/" + encodeURI(encodeURIComponent(searchTerm));
            return false;
        });

        $("#omni-input").popover({
           placement: "bottom",
           trigger: "hover",
           html: true,
           title: function() {
                $(this).attr("title");
           },
           content: function() {
               return $("#search-help-content").html();
           },
           delay: {hide: 2000}
        });

        $("a.help-navigate").click(function(e) {
            e.preventDefault();
            (new HelpNavigateView()).render();
        });
    });

}(window.jQuery);
