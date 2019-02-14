(function ($) {

    // These seperators are for replacing items within the observation summary
    var leftSep = "<";
    var rightSep = ">";

    // This is for the moustache-like templates
    // prevents collisions with JSP tags <%...%>
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };

    /* Views */
    var HelpNavigateView = Backbone.View.extend({
        template: _.template($("#help-navigate-tmpl").html()),

        render: function() {
            var content = this.template({});

            $.fancybox.open(
                content,
                {
                    'autoDimensions' : false,
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
       };
     
       var showInvalidMessage = function(message)
       {    	 
    	  $("#alertMessage").text(message);
    	  $("#alertMessage").css('color', 'red');
    	  $("#alert-message-modal").modal('show');
       };

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
            new __TemplateHelperView().render();
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
