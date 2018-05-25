(function ($) {

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

    /* Routers */
    AppRouter = Backbone.Router.extend({
        routes: {
            "template-helper": "showTemplateHelper",
            "about": "helpNavigate",
            "help-navigate": "helpNavigate",
            "*actions": "showTemplateHelper"
        },

        helpNavigate: function() {
            var helpNavigateView = new HelpNavigateView();
            helpNavigateView.render();
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
           trigger: "manual",
           html: true,
           title: function() {
                $(this).attr("title");
           },
           content: function() {
               return $("#search-help-content").html();
           },
        }).on("mouseenter", function () {
            var _this = this;
            $(this).popover("show");
            $(".popover").on("mouseleave", function () {
                $(_this).popover('hide');
            });
        }).on("mouseleave", function () {
            var _this = this;
            setTimeout(function () {
                if (!$(".popover:hover").length) {
                    $(_this).popover("hide");
                }
            }, 300);
        });

        $("a.help-navigate").click(function(e) {
            e.preventDefault();
            (new HelpNavigateView()).render();
        });
    });

})(window.jQuery);
