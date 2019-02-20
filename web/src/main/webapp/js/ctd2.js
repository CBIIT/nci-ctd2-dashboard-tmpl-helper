(function ($) {

    // This is for the moustache-like templates
    // prevents collisions with JSP tags <%...%>
    _.templateSettings = {
        interpolate: /\{\{(.+?)\}\}/g
    };

    /* Views */
    const HelpNavigateView = Backbone.View.extend({
        template: _.template($("#help-navigate-tmpl").html()),

        render: function () {
            const content = this.template({});

            $.fancybox.open(
                content, {
                    'autoDimensions': false,
                    'centerOnScroll': true,
                    'transitionIn': 'none',
                    'transitionOut': 'none'
                }
            );

            return this;
        }
    });

    /* Routers */
    const AppRouter = Backbone.Router.extend({
        routes: {
            "template-helper": "showTemplateHelper",
            "about": "helpNavigate",
            "help-navigate": "helpNavigate",
            "*actions": "showTemplateHelper"
        },

        helpNavigate: function () {
            new HelpNavigateView().render();
        },

        showTemplateHelper: function () {
            new __TemplateHelperView().render();
        }
    });

    $(function () {
        new AppRouter();
        Backbone.history.start();

        $("#omnisearch").submit(function () {
            const searchTerm = $("#omni-input").val();
            window.location = "/dashboard/#search/" + encodeURI(encodeURIComponent(searchTerm));
            return false;
        });

        $("#omni-input").popover({
            placement: "bottom",
            trigger: "manual",
            html: true,
            title: function () {
                $(this).attr("title");
            },
            content: function () {
                return $("#search-help-content").html();
            },
        }).on("mouseenter", function () {
            const _this = this;
            $(this).popover("show");
            $(".popover").on("mouseleave", function () {
                $(_this).popover('hide');
            });
        }).on("mouseleave", function () {
            const _this = this;
            setTimeout(function () {
                if (!$(".popover:hover").length) {
                    $(_this).popover("hide");
                }
            }, 300);
        });

        $("a.help-navigate").click(function (e) {
            e.preventDefault();
            (new HelpNavigateView()).render();
        });
    });

})(window.jQuery);