const __TemplateHelperView = (function ($) {

    /* the main state variables */
    let centerId = 0; // ID of the center currently selected
    let templateModels = {}; // data of all templates, keyed by their ID's
    let currentModel = null; // currently selected submission template, or null meaning no template selected
    let saveSuccess = true;
    let defaultPis = {};

    /* variables supporting observation data, especially file attachment */
    let observationArray = [];
    let file_number = 0;
    let finished_file_number = 0;

    /* models */
    const SubmissionCenters = Backbone.Collection.extend({
        url: "./api/centers"
    });

    const SubmissionTemplate = Backbone.Model.extend({
        defaults: {
            id: 0,
            firstName: null,
            lastName: null,
            email: null,
            phone: null,
            displayName: null,
            description: null,
            project: null,
            tier: null,
            isStory: null,
            storyTitle: null,
            subjectColumns: [],
            subjectClasses: [],
            evidenceColumns: [],
            evidenceTypes: [],
            valueTypes: [],
            observationNumber: 0,
            observations: "",
            summary: "",
            piName: "",
        },
        getPreviewModel: function (obvIndex) {
            // re-structure the data for the preview, required by the original observation template
            const obj = this.toJSON();

            const observationTemplate = {
                tier: obj.tier,
                submissionCenter: obj.submissionCenter,
                project: obj.project,
                submissionDescription: obj.description,
                observationSummary: obj.summary,
                ECOCode: obj.ecoCodes,
            };

            const subjectColumns = obj.subjectColumns;
            const evidenceColumns = obj.evidenceColumns;
            const observations = obj.observations;
            const totalRows = subjectColumns.length + evidenceColumns.length;

            const observedSubjects = [];
            for (let i = 0; i < subjectColumns.length; i++) {
                observedSubjects.push({
                    subject: {
                        id: 0, // TODO proper value needed for the correct image? 
                        class: obj.subjectClasses[i],
                        displayName: observations[totalRows * obvIndex + i],
                    },
                    id: i, //'SUBJECT ID placeholder', // depend on the man dashboard. for image?
                    observedSubjectRole: {
                        columnName: subjectColumns[i],
                        subjectRole: {
                            displayName: obj.subjectRoles[i],
                        },
                        displayText: obj.subjectDescriptions[i],
                    }
                });
            }
            const observedEvidences = [];
            for (let i = 0; i < obj.evidenceColumns.length; i++) {
                const numericValue = observations[totalRows * obvIndex + subjectColumns.length + i];
                if (obj.valueTypes[i] == 'numeric' || obj.valueTypes[i] == 'label')
                    evidenceValue = numericValue;
                else
                    evidenceValue = ""; // do not display for other value types
                observedEvidences.push({
                    evidence: {
                        id: 0, // TODO usage?
                        class: obj.valueTypes[i],
                        displayName: evidenceValue, // this is needed in preview of observation summry
                        filePath: '', // TODO when needed
                        mimeType: '', // TODO add when needed
                        url: '', // TODO add when needed
                        numericValue: numericValue, // This is put in the Details column in the preview.
                    },
                    id: i, // TODO usage?
                    observedEvidenceRole: {
                        columnName: evidenceColumns[i],
                        evidenceRole: {
                            displayName: obj.evidenceTypes[i],
                        },
                        displayText: obj.evidenceDescriptions[i],
                    },
                    displayName: observations[totalRows * obvIndex + subjectColumns.length + i], // This is put in the Details column in the preview.
                });
            }

            return {
                id: obvIndex + 1,
                submission: {
                    id: 0, // this field is used detail-detail. 0 in effect disables it
                    observationTemplate: observationTemplate,
                    submissionDate: obj.dateLastModified,
                    displayName: obj.displayName,
                },
                observedSubjects: observedSubjects,
                observedEvidences: observedEvidences,
            };
        },
    });

    const StoredTemplates = Backbone.Collection.extend({
        url: "api/templates/",
        model: SubmissionTemplate,
        initialize: function (attributes) {
            this.url += attributes.centerId;
        }
    });

    /* viewes */
    const TemplateHelperView = Backbone.View.extend({
        template: _.template($("#template-helper-tmpl").html()),
        el: $("#main-container"),

        render: function () {
            $(this.el).html(this.template(this.model));

            // top menu
            $("#menu_home").click(function () {
                centerId = 0;
                templateModels = {};
                currentModel = null;
                $("#menu_manage").hide();
                hideTemplateMenu();
                showPage("#center-select-page");
            });
            $("#menu_manage").click(function () {
                currentModel = null;
                hideTemplateMenu();
                showPage("#submission-list-page");
            }).hide();
            $("#menu_description").click(function () {
                showPage("#description-page", this);
            }).hide();
            $("#menu_data").click(function () {
                showPage("#submission-data-page", this);
            }).hide();
            $("#menu_summary").click(function () {
                populateTagList();
                showPage("#observation-summary-page", this);
            }).hide();
            $("#menu_preview").click(function () {
                showPage("#preview-page", this);
            }).hide();

            const submissionCenters = new SubmissionCenters();
            submissionCenters.fetch({
                success: function () {
                    defaultPis = {};
                    _.each(submissionCenters.models, function (aCenter) {
                        const centerModel = aCenter.toJSON();
                        defaultPis[centerModel.id] = centerModel.piName;
                        (new TemplateHelperCenterView({
                            model: centerModel,
                            el: $("#template-submission-centers")
                        })).render();
                    });
                }
            });

            $("#upload-new-submission").click(function () {
                /* this looks similar to "create-new-submission", but really serves completely different purpose.
                This view is to catch the basic submission/submitter information for a submisstion to be validated;
                the submission itself is NOT to be imported to the database.
                */
                hideTemplateMenu();
                if (currentModel != null) {
                    console.log('error: unexpected non-null currentModel');
                    return;
                }

                // this basically has nothing in common with the template model (currentModel) in this app.
                const validationSubmissionModel = new ValidationSubmission();

                (new ValidationSubmissionDescriptionView({
                    model: validationSubmissionModel,
                    el: $("#validation-submission-description")
                })).render();

                $("#submission-list-page").fadeOut();
                $("#upload-view").slideDown();
            });

            $("#apply-submission-center").click(function () {
                const centerId_selected = $("#template-submission-centers").val();
                if (centerId_selected.length == 0) {
                    console.log("centerId_selected is empty");
                    return; // error control
                }
                centerId = centerId_selected;

                $("#menu_manage").show();
                $("#center-select-page").fadeOut();
                $("#submission-list-page").slideDown();
                $("span#center-name").text($("#template-submission-centers option:selected").text());
                refreshTemplateList();
            });

            $("#create-new-submission").click(function () {
                hideTemplateMenu();
                if (currentModel != null) {
                    console.log('error: unexpected non-null currentModel');
                    return;
                }
                populateOneTemplate(); // TODO maybe use a separate method for the case of new template

                $("#submission-list-page").fadeOut();
                $("#description-page").slideDown();
            });

            // although the other button is called #create-new-submission, this is where it is really created back-end
            $("#save-name-description").click(function () {
                if (currentModel.id == 0) {
                    $(this).attr("disabled", "disabled");
                    saveNewTemplate(true);
                } else {
                    update_model_from_description_page($(this));
                }
            });
            $("#continue-to-main-data").click(function () { // similar to save, additionally moving to the next
                let ret = true;
                if (currentModel.id == 0) {
                    ret = saveNewTemplate(false);
                } else {
                    update_model_from_description_page($(this));
                }
                if (ret && saveSuccess) {
                    $("#description-page").fadeOut();
                    $("#submission-data-page").slideDown();
                    $("#menu_description").removeClass("current-page");
                    $("#menu_data").addClass("current-page");
                } else {
                    saveSuccess = true; // reset the flag
                }
            });

            $("#save-template-submission-data").click(update_model_from_submission_data_page);
            $("#apply-template-submission-data").click(update_model_from_submission_data_page);

            $("#template-obs-summary").change(function () {
                console.log('change triggered on summary');
            });
            $("#save-summary").click(function () {
                console.log("saving the summary ...");
                update_model_from_summary_page($(this)); // TODO add lock
            });
            $("#continue-from-summary").click(function () {
                update_model_from_summary_page($(this));
                if (saveSuccess) {
                    $("#observation-summary-page").fadeOut();
                    $("#preview-page").slideDown();
                    $("#menu_summary").removeClass("current-page");
                    $("#menu_preview").addClass("current-page");
                } else {
                    saveSuccess = true; // reset the flag
                }
            });

            $("#add-evidence").click(function () {
                addNewEvidence();
            });

            $("#add-subject").click(function () {
                addNewSubject();
            });

            $("#add-observation").click(function () {
                new NewObservationView({
                    el: $("#template-table"),
                }).render();
            });

            const common_ecoterms = [];
            for (let i = 0; i < ecoterms.length; i++) {
                const e = ecoterms[i];
                common_ecoterms.push(['', e[0] + ' ' + e[1], '<a href="http://www.evidenceontology.org/browse/#' + e[0] + '" target="_blank">link</a>', e[2]]);
            }
            $('#definition-box').hide();
            const table = $("#common-ecoterms").DataTable({
                data: common_ecoterms,
                columns: [{
                        title: "Select",
                    },
                    {
                        title: "Evidence Ontology code and name",
                    },
                    {
                        title: "Details",
                    },
                ],
                "scrollY": "200px",
                "scrollCollapse": true,
                "paging": false,
                "ordering": false,
                "info": false,
                "searching": false,
                "autoWidth": false,
                columnDefs: [{
                    orderable: false,
                    className: 'select-checkbox',
                    targets: 0
                }],
                select: {
                    style: 'multi',
                    selector: 'td:first-child'
                },
            }).on('click', 'tbody td', function () {
                if (this.cellIndex == 1) {
                    const row = this.parentNode.rowIndex - 1;
                    $('#ecoterm-name').text(this.textContent);
                    $('#ecoterm-definition').text(table.data()[row][3]);
                    $('#definition-box').show();
                }
            });
            // trick to get dataTables head width right
            const header = $("#common-ecoterms").parent().parent().find('table');
            header.parent().width("100%");
            header.width("100%");

            $("#open-additional-ecoterms").click(function () {
                $("#additional-ecoterms").show();
            });

            $("#download-form").submit(function () {
                if (!$("#template-id").val() || $("#template-id").val() == "0") {
                    $("#template-id").val(currentModel.id);
                }
                const model = templateModels[$("#template-id").val()];
                $("#filename-input").val(model.toJSON().displayName);
                return true;
            });

            $("#download-from-preview").click(function () {
                $("#download-form").submit();
            });

            $("#validate-from-preview").click(function () {
                validate_interal_template(currentModel.id);
            });

            $('.desc-tooltip').popover({
                placement: "bottom",
                trigger: "hover",
            });

            return this;
        } // end render function
    }); // end of TemplateHelperView

    const ObservationPreviewView = Backbone.View.extend({
        template: _.template($("#observation-tmpl").html()),
        render: function () {
            const thisModel = this.model;
            const observationId = 'observation-preview-' + thisModel.id;
            const observation_preview = this.template(thisModel)
                .replace('id="observation-container"', 'id="' + observationId + '"')
                .replace('<h2>Observation', '<h2>Observation ' + thisModel.id);
            $(this.el).html(observation_preview);
            $('#' + observationId).css('display', thisModel.display);

            // We will replace the values in this summary
            let summary = thisModel.submission.observationTemplate.observationSummary;

            // Load Subjects
            const thatEl = $("#" + observationId + " #observed-subjects-grid");
            _.each(thisModel.observedSubjects, function (observedSubject) {
                const observedSubjectRowView = new ObservedSubjectSummaryRowView({
                    el: $(thatEl).find("tbody"),
                    model: observedSubject
                });
                observedSubjectRowView.render();

                const subject = observedSubject.subject;
                const thatEl2 = $("#" + observationId + " #subject-image-" + observedSubject.id);
                let imgTemplate = $("#search-results-unknown-image-tmpl");
                if (subject.class == "gene") {
                    imgTemplate = $("#search-results-gene-image-tmpl");
                } else if (subject.class == "shrna") {
                    imgTemplate = $("#search-results-shrna-image-tmpl");
                } else if (subject.class == "tissue_sample") {
                    imgTemplate = $("#search-results-tissuesample-image-tmpl");
                } else if (subject.class == "cell_sample") {
                    imgTemplate = $("#search-results-cellsample-image-tmpl");
                } else if (subject.class == "animal_model") {
                    imgTemplate = $("#search-results-animalmodel-image-tmpl");
                }
                /* in the dashboard app, for Compound, this would be set asynchronously and use compound instead of subject */
                thatEl2.append(_.template(imgTemplate.html())(subject));
                $('.img-polaroid').popover({
                    placement: "left",
                    trigger: "hover",
                });

                if (observedSubject.observedSubjectRole == null || observedSubject.subject == null)
                    return;

                summary = summary.replace(
                    new RegExp("<" + observedSubject.observedSubjectRole.columnName + ">", "g"),
                    _.template($("#summary-subject-replacement-tmpl").html())(observedSubject.subject)
                );
            });

            const ecoTable = $("#eco-grid");
            const ecocodes = thisModel.submission.observationTemplate.ECOCode;
            if (ecocodes == null || ecocodes.length == 0) {
                ecoTable.hide();
            } else {
                const ecos = ecocodes.split('|');
                const ecodata = [];
                ecos.forEach(function (ecocode) {
                    if (ecocode == '') return;
                    $.ajax({
                        async: false,
                        url: "/dashboard/api/eco/" + ecocode,
                        type: "GET",
                        success: function (response) {
                            ecodata.push(['<a>' + ecocode + '</a>', response.name]);
                        },
                        error: function (response, status) {
                            $('#validation-progress').modal('hide');
                            console.log(response);
                            console.log(status);
                        },
                    });
                });

                ecoTable.DataTable({
                    data: ecodata,
                    paging: false,
                    ordering: false,
                    info: false,
                    searching: false,
                });
            }

            // Load evidences
            const thatEl2 = $("#" + observationId + " #observed-evidences-grid");
            _.each(thisModel.observedEvidences, function (observedEvidence) {
                const observedEvidenceRowView = new ObservedEvidenceRowView({
                    el: $(thatEl2).find("tbody"),
                    model: observedEvidence
                });

                observedEvidenceRowView.render();
                summary = summary.replace(
                    new RegExp("<" + observedEvidence.observedEvidenceRole.columnName + ">", "g"),
                    _.template($("#summary-evidence-replacement-tmpl").html())(observedEvidence.evidence)
                );
            });
            $("#" + observationId + " #observation-summary").html(summary);

            $('.desc-tooltip').popover({
                placement: "left",
                trigger: "hover",
            });

            $(".numeric-value").each(function () {
                const val = $(this).html();
                const vals = val.split("e"); // capture scientific notation
                if (vals.length > 1) {
                    $(this).html(_.template($("#observeddatanumericevidence-val-tmpl").html())({
                        firstPart: vals[0],
                        secondPart: vals[1].replace("+", "")
                    }));
                }
            });

            $(".no-preview").removeAttr('href');

            $("#small-show-sub-details").click(function (event) {
                event.preventDefault();
                $("#obs-submission-details").slideDown();
                $("#small-show-sub-details").hide();
                $("#small-hide-sub-details").show();
            });

            $("#small-hide-sub-details").click(function (event) {
                event.preventDefault();
                $("#obs-submission-details").slideUp();
                $("#small-hide-sub-details").hide();
                $("#small-show-sub-details").show();
            });

            if (thisModel.submission.observationTemplate.submissionDescription == "") {
                $("#obs-submission-summary").hide();
            }

            return this;
        }
    });

    // this is the similar to the one in dashboard ctd2.js
    const ObservedSubjectSummaryRowView = Backbone.View.extend({
        template: _.template($("#observedsubject-summary-row-tmpl").html()),
        render: function () {
            const result = this.model;
            if (result.subject == null) return;
            if (result.subject.type == undefined) {
                result.subject.type = result.subject.class;
            }

            if (result.subject.class != "gene") {
                this.template = _.template($("#observedsubject-summary-row-tmpl").html());
                $(this.el).append(this.template(result));
            } else {
                this.template = _.template($("#observedsubject-gene-summary-row-tmpl").html());
                $(this.el).append(this.template(result));
            }

            return this;
        }
    });

    // this is the same as the one in dashboard ctd2.js for now
    const ObservedEvidenceRowView = Backbone.View.extend({
        render: function () {
            const result = this.model;
            const type = result.evidence.class;
            result.evidence.type = type;

            if (result.observedEvidenceRole == null) {
                result.observedEvidenceRole = {
                    displayText: "-",
                    evidenceRole: {
                        displayName: "unknown"
                    }
                };
            }

            let templateId = "#observedevidence-row-tmpl";
            let isHtmlStory = false;
            if (type == "file") {
                result.evidence.filePath = result.evidence.filePath.replace(/\\/g, "/");
                templateId = "#observedfileevidence-row-tmpl";
            } else if (type == "url") {
                templateId = "#observedurlevidence-row-tmpl";
            } else if (type == "label") {
                templateId = "#observedlabelevidence-row-tmpl";
            } else if (type == "numeric") {
                templateId = "#observeddatanumericevidence-row-tmpl";
            }

            this.template = _.template($(templateId).html());
            const thatEl = $(this.el);
            $(this.el).append(this.template(result));

            if (isHtmlStory) {
                thatEl.find(".html-story-link").on("click", function (e) {
                    e.preventDefault();
                    (new HtmlStoryView({
                        model: {
                            observation: result.observation,
                            url: $(this).attr("href"),
                        }
                    })).render();
                });
            }

            $(".img-rounded").popover({
                placement: "left",
                trigger: "hover",
            });
            return this;
        }
    });

    const ObservationOptionView = Backbone.View.extend({
        template: _.template($("#observation-option-tmpl").html()),
        render: function () {
            $(this.el).append(this.template(this.model));
            return this;
        }
    });

    const ColumnTagView = Backbone.View.extend({
        template: _.template($("#column-tag-item-tmpl").html()),
        render: function () {
            $(this.el).append(this.template(this.model));
            return this;
        }
    });

    const TemplateHelperCenterView = Backbone.View.extend({
        template: _.template($("#template-helper-center-tmpl").html()),

        render: function () {
            $(this.el).append(this.template(this.model));
            return this;
        }
    });

    const SubmitterInformationView = Backbone.View.extend({
        template: _.template($("#submitter-information-tmpl").html()),

        render: function () {
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        events: {
            change: function () {
                console.log('change triggered on submitter information');
            }
        },
    });

    const TemplateDescriptionView = Backbone.View.extend({
        template: _.template($("#template-description-tmpl").html()),

        render: function () {
            $(this.el).html(this.template(this.model.toJSON()));
            if ($("#template-is-story").is(':checked')) $('#story-title-row').show();
            else $('#story-title-row').hide();
            $("#template-is-story").change(function () {
                if ($(this).is(':checked')) $('#story-title-row').show();
                else $('#story-title-row').hide();
            });
            return this;
        },
        events: {
            change: function () {
                console.log('change triggered on template description');
            }
        },
    });

    const ValidationSubmissionDescriptionView = Backbone.View.extend({
        template: _.template($("#validation-submission-description-tmpl").html()),

        render: function () {
            $(this.el).html(this.template(this.model.toJSON()));

            $("#upload-zip-file").change(function () {
                /*
                1. save the submission information (completely separated from the regular submission information for the templates entered online)
                2. upload zip file
                3. create the 'submission package' (text files required by the validation Python script)
                4. run the validation script
                5. create the report
                */
                uploadZip(this);
            });

            return this;
        },
        events: {
            change: function () {
                console.log('change triggered on validation submission description');
            }
        },
    });

    const ValidationReportView = Backbone.View.extend({
        template: _.template($("#validation-report-tmpl").html()),
        render: function () {
            $.fancybox.open(this.template(this.model));

            const templateId = this.model.templateId;
            $("#download-report").unbind('click').click(function () {
                location.href = 'download/report?centerId=' + centerId + '&templateId=' + templateId;
            });
            return this;
        }
    });

    const ExistingTemplateView = Backbone.View.extend({
        template: _.template($("#existing-template-row-tmpl").html()),

        render: function () {
            $(this.el).append(this.template(this.model.toJSON()));
            const templateId = this.model.id;
            $("#template-action-" + templateId).change(function () {
                const action = $(this).val();
                switch (action) {
                    case 'edit':
                        showTemplateMenu();
                        currentModel = templateModels[templateId];
                        populateOneTemplate();
                        showPage("#submission-data-page", "#menu_data");
                        break;
                    case 'delete':
                        deleteTemplate(templateId);
                        $("#template-action-" + templateId).val(""); // in case not confirmed 
                        break;
                    case 'preview':
                        showTemplateMenu();
                        currentModel = templateModels[templateId];
                        populateOneTemplate();
                        showPage("#preview-page", "#menu_preview");
                        break;
                    case 'clone':
                        clone(templateId);
                        break;
                    case 'download':
                        $("#template-id").val(templateId);
                        $("#download-form").submit();
                        break;
                    case 'validate':
                        validate_interal_template(templateId);
                        break;
                    default:
                        /* this should never happen */
                        alert('template #' + templateId + ': action ' + action + ' clicked');
                }
                $(this).val('');
            });
            return this;
        }
    });

    const TemplateSubjectDataRowView = Backbone.View.extend({
        template: _.template($("#template-subject-data-row-tmpl").html()),
        render: function () {
            $(this.el).append(this.template(this.model));
            const columnTagId = this.model.columnTagId;
            $("#delete-subject-" + columnTagId).click(function () {
                $("#confirmed-delete").unbind('click').click(function () {
                    $('tr#template-subject-row-columntag-' + columnTagId).remove();
                });
                $('#confirmation-message').text("Are you sure you want to delete this subject row?");
                $("#confirmation-modal").modal('show');
            });

            let subjectClass = this.model.subjectClass || "gene"; // simple default value

            const resetRoleDropdown = function (sc, sr) {
                const subject2role = {
                    'gene': ['target', 'biomarker', 'oncogene', 'perturbagen', 'regulator', 'interactor', 'context'],
                    'shrna': ['perturbagen'],
                    'tissue_sample': ['metastasis', 'disease', 'tissue'],
                    'cell_sample': ['cell line'],
                    'compound': ['drug', 'perturbagen', 'metabolite', 'control', 'probe'],
                    'animal_model': ['strain'],
                };

                roleOptions = subject2role[sc];
                if (roleOptions === undefined) { // exceptional case
                    console.log("error: roleOption is undefined for subject class " + sc);
                    // because this happens for previous stored data, let's allow this
                    //return;
                    roleOptions = subject2role.gene;
                }
                $('#role-dropdown-' + columnTagId).empty();
                for (let i = 0; i < roleOptions.length; i++) {
                    const roleName = roleOptions[i];
                    new SubjectRoleDropdownRowView({
                        el: $('#role-dropdown-' + columnTagId),
                        model: {
                            roleName: roleName,
                            selected: roleName == sr ? 'selected' : null
                        }
                    }).render();
                }
            };
            resetRoleDropdown(subjectClass, this.model.subjectRole);
            $('#subject-class-dropdown-' + columnTagId).change(function () {
                resetRoleDropdown($(this).val(), null);
            });

            // render observation cells for one row (subject or evidence column tag)
            new TempObservationView({
                el: $('#template-subject-row-columntag-' + columnTagId),
                model: {
                    columnTagId: columnTagId,
                    observationNumber: this.model.observationNumber,
                    observations: this.model.observations,
                    obvsType: 'text'
                },
            }).render();

            $(this.el).find(".collapsed-textarea").click(popupLargeTextfield);
            $("#close-tempoary-text").unbind('click').click(closeLargeTextfield);

            return this;
        },
        events: {
            change: function () {
                console.log('change triggered on subject data');
            }
        },
    });

    const TemplateEvidenceDataRowView = Backbone.View.extend({
        template: _.template($("#template-evidence-data-row-tmpl").html()),
        render: function () {
            $(this.el).append(this.template(this.model));
            const columnTagId = this.model.columnTagId;
            $("#delete-evidence-" + columnTagId).click(function () {
                $("#confirmed-delete").unbind('click').click(function () {
                    $('tr#template-evidence-row-columntag-' + columnTagId).remove();
                });
                $('#confirmation-message').text("Are you sure you want to delete this evidence row?");
                $("#confirmation-modal").modal('show');
            });

            const resetEvidenceTypeDropdown = function (vt, et) {
                const value_type2evidence_type = {
                    'numeric': ['measured', 'observed', 'computed', 'background'],
                    'label': ['measured', 'observed', 'computed', 'species', 'background'],
                    'file': ['literature', 'measured', 'observed', 'computed', 'written', 'background'],
                    'url': ['measured', 'computed', 'reference', 'resource', 'link'],
                };

                const evidenceTypeOptions = value_type2evidence_type[vt] || value_type2evidence_type.numeric; // default to numeric, though it should not happen
                $('#evidence-type-' + columnTagId).empty();
                for (let i = 0; i < evidenceTypeOptions.length; i++) {
                    new EvidenceTypeDropdownView({
                        el: $('#evidence-type-' + columnTagId),
                        model: {
                            evidenceType: evidenceTypeOptions[i],
                            selected: evidenceTypeOptions[i] == et
                        }
                    }).render();
                }
            };
            resetEvidenceTypeDropdown(this.model.valueType, this.model.evidenceType);

            // render observation cells for one row (evidence column tag)
            const tableRow = $('#template-evidence-row-columntag-' + columnTagId);
            new TempObservationView({
                el: tableRow,
                model: {
                    columnTagId: columnTagId,
                    observationNumber: this.model.observationNumber,
                    observations: this.model.observations,
                    obvsType: this.model.valueType
                },
            }).render();

            tableRow.find('.value-types').change(function () {
                const fields = $('#template-evidence-row-columntag-' + columnTagId + " [id^=observation-]");
                const new_type = $(this).val();
                if (fields.length > 0 && new_type != fields[0].type) {
                    resetEvidenceTypeDropdown(new_type, null);
                    for (let i = 0; i < fields.length; i++) {
                        fields[i].type = new_type;
                        $(fields[i]).val('');
                        $(fields[i]).parent().find(".uploaded").empty();
                    }
                }
            });

            $(this.el).find(".collapsed-textarea").click(popupLargeTextfield);
            $("#close-tempoary-text").unbind('click').click(closeLargeTextfield);

            return this;
        },
        events: {
            change: function () {
                console.log('change triggered on evidence data');
            }
        },
    });

    const SubjectRoleDropdownRowView = Backbone.View.extend({
        template: _.template($('#role-dropdown-row-tmpl').html()),
        render: function () {
            // the template expects roleName, selected, cName from the model
            $(this.el).append(this.template(this.model));
        }
    });

    const EvidenceTypeDropdownView = Backbone.View.extend({
        template: _.template($('#evidence-type-dropdown-tmpl').html()),
        render: function () {
            $(this.el).append(this.template(this.model));
        }
    });

    /* This view's model covers observation data of one row (i.e. subject column tag),
     * but the template is for individual cells 
     * so the template's own model contains individual cell's data.
     * This is necessary because the number of observations, and thus the column number, is a variable. 
     */
    const TempObservationView = Backbone.View.extend({
        template: _.template($("#temp-observation-tmpl").html()),
        render: function () {
            const obvModel = this.model;

            const clear_uploaded = function () {
                $(this).parent().find(".uploaded").empty();
            };

            for (let column = 0; column < obvModel.observationNumber; column++) {
                const obvContent = obvModel.observations[column];
                let u = '';
                if (obvModel.obvsType == 'file') {
                    if (obvContent === undefined || obvContent == null || obvContent == "undefined" ||
                        (obvContent.length > 200 && obvContent.includes("base64:"))) {
                        // don't display if it is the content intead of the filename
                    } else {
                        // remove meme-type string
                        const mime_index = obvContent.indexOf("::");
                        if (mime_index > 0) {
                            u = obvContent.substring(0, mime_index);
                        } else {
                            u = obvContent;
                        }
                        const i1 = u.lastIndexOf('\\');
                        if (i1 >= 0) u = u.substring(i1 + 1);
                        const i2 = u.lastIndexOf('/');
                        if (i2 >= 0) u = u.substring(i2 + 1);
                    }
                }
                const cellModel = {
                    obvNumber: column,
                    obvColumn: obvModel.columnTagId,
                    obvText: escapeQuote(obvContent),
                    type: obvModel.obvsType,
                    uploaded: u
                };
                $(this.el).append(this.template(cellModel));
                $(this.el).find("input").change(clear_uploaded);
            }

        }
    });

    /* this is one NEW column in the observation data table. because it is new, it is meant to be empty */
    const NewObservationView = Backbone.View.extend({
        template: _.template($("#temp-observation-tmpl").html()),
        render: function () {
            const tmplt = this.template;
            const obvNumber = $('#template-table tr#subject-header').find('th').length - 4;
            let columnTagId = 0;
            $(this.el).find("tr.template-data-row").each(function () {
                const obvTemp = tmplt({
                    obvNumber: obvNumber,
                    obvColumn: columnTagId,
                    obvText: null,
                    type: $(this).find(".value-types").val(),
                    uploaded: ""
                });
                $(this).append(obvTemp);
                columnTagId++;
            });
            const deleteButton = "delete-column-" + obvNumber;
            $(this.el).find("tr#subject-header").append("<th class=observation-header>Observation " + obvNumber + "<br>(<button class='btn btn-link' id='" + deleteButton + "'>delete</button>)</th>");
            $(this.el).find("tr#evidence-header").append("<th class=observation-header>Observation " + obvNumber + "</th>");
            $("#" + deleteButton).click(function () {
                const c = $('#template-table tr#subject-header').find('th').index($(this).parent());
                $('#template-table tr').find('td:eq(' + c + '),th:eq(' + c + ')').remove();
            });
            $(this.el).parent().scrollLeft($(this.el).width());
        }
    });

    /* support functions */
    const validate_interal_template = function (templateId) {
        /* 
        1. create ZIP file (asked by requirement document, but why?) 
        2. create "submission package" (as in the text files required by the validation Python script)
        3. run the validation script
        4. create the report
        */
        $('#validation-progress').modal('show');
        $.ajax({
            async: true,
            url: "template/validate",
            type: "GET",
            data: jQuery.param({
                templateId: templateId,
            }),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (response) {
                $('#validation-progress').modal('hide');
                response.templateId = templateId;
                (new ValidationReportView({
                    model: response,
                })).render();
            },
            error: function (response, status) {
                $('#validation-progress').modal('hide');
                console.log(response);
                console.log(status);
            },
        });
    };

    const read_description_page = function () {
        const firstName = $("#first-name").val();
        const lastName = $("#last-name").val();
        const email = $("#email").val();
        const phone = $("#phone").val();
        const displayName = $("#template-name").val();
        const description = $("#template-submission-desc").val();
        const project = $("#template-project-title").val();
        const tier = $("#template-tier").val();
        const isStory = $("#template-is-story").is(':checked');
        const storyTitle = $('#story-title').val();
        const piName = $('#pi-name').val();

        if (firstName.length > 255) {
            return {
                error: "The first name field is too long. Its length is limited to 255."
            };
        }
        if (lastName.length > 255) {
            return {
                error: "The last name field is too long. Its length is limited to 255."
            };
        }
        if (email.length > 255) {
            return {
                error: "The email field is too long. Its length is limited to 255."
            };
        }
        if (phone.length > 255) {
            return {
                error: "The phone field is too long. Its length is limited to 255."
            };
        }
        if (displayName.length > 128) {
            return {
                error: "The submission name field is too long. Its length is limited to 128."
            };
        }

        if (description.length > 1024) {
            return {
                error: "The description field is too long. Its length is limited to 1024."
            };
        }
        if (project.length > 1024) {
            return {
                error: "The project field is too long. Its length is limited to 1024."
            };
        }
        if (storyTitle.length > 1024) {
            return {
                error: "The story title field is too long. Its length is limited to 1024."
            };
        }
        if (piName.length > 64) {
            return {
                error: "The PI name field is too long. Its length is limited to 64."
            };
        }

        return {
            firstName: firstName,
            lastName: lastName,
            email: email,
            phone: phone,
            displayName: displayName,
            description: description,
            project: project,
            tier: tier,
            isStory: isStory,
            storyTitle: storyTitle,
            piName: piName,
        };
    };

    const update_model_from_description_page = function (triggeringButton) {

        const description_data = read_description_page();
        if (description_data.error) {
            showAlertMessage(description_data.error);
            return;
        }

        currentModel.set(description_data);

        triggeringButton.attr("disabled", "disabled");
        $.ajax({
            url: "template/update-description",
            async: false,
            type: "POST",
            data: jQuery.param(currentModel.toJSON()),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (data) {
                console.log("return value: " + data);
                triggeringButton.removeAttr("disabled");
                refreshTemplateList();
                currentModel = templateModels[currentModel.id]; // the values are not changed but it is a different object after refreshing
                updatePreview();
            },
            error: function (response, status) {
                triggeringButton.removeAttr("disabled");
                console.log(status + ": " + response.responseText);
                showInvalidMessage("The template description data was NOT saved to the server for some unexpected error. " +
                    "Please contact the administrator of this application to help finding out the specific cause and fixing it. " +
                    "Sorry for the inconvenience.");
            }
        });

    };

    // it is not evident why and how we use this data object yet
    const ValidationSubmission = Backbone.Model.extend({
        defaults: {
            firstName: "John",
            lastName: "Doe",
            email: null,
            phone: null,
            displayName: null,
            isResubmission: false,
        },
    });

    const validate_column_tags = function (tags, tag_name) {
        if (!Array.isArray(tags)) {
            console.log("ERROR: input object that is not an array");
            return '';
        }

        const pattern = /^[a-z0-9]+(_[a-z0-9]+)*$/;
        const tmp = [];

        for (let i = 0; i < tags.length; i++) {
            if (tags[i] == null || tags[i] == "") {
                return tag_name + " is empty";
            }
            if (tags[i].length > 1024) {
                return tag_name + " (" + tags[i].length + " characters) is longer than allowed 1024 characters";
            }
            if (!pattern.test(tags[i])) {
                return tag_name + " '" + tags[i] + "' does not follow the convention of underscore-separated lowercase letters or digits)</li>";
            }

            if (tmp.indexOf(tags[i]) >= 0) {
                return "duplicate item: " + tags[i];
            }
            tmp.push(tags[i]);
        }
        return '';
    };

    const validate_description = function (descriptions, description_name) {
        if (!Array.isArray(descriptions)) {
            console.log("ERROR: input object that is not an array");
            return '';
        }

        const normalCharacaters = /^[ -~\t\n\r]+$/;
        for (let i = 0; i < descriptions.length; i++) {
            if (descriptions[i] == null || descriptions[i] == "") {
                return description_name + " is empty";
            }
            if (descriptions[i].length > 10240) {
                return description_name + " (" + descriptions[i].length + " characters) is longer than allowed 10240 characters";
            }
            if (!normalCharacaters.test(descriptions[i])) {
                const non_regular = /([ -~\t\n\r]{0,5})([^ -~\t\n\r])(.{0,5})/.exec(descriptions[i]);
                return description_name + " contains characters beyond 7-bit ASCII: " + non_regular[1] + "<font color=red>" + non_regular[2] + "</font>" + non_regular[3];
            }
        }
        return '';
    };

    /* this only checks length of gene symbol for now. nonetheless, it is complicated because of using observationArray */
    const validate_observation = function (subject_classes, observation_number) {
        const rows = $("#template-table tr").length - 4; // subjects + evidences
        for (let i = 0; i < subject_classes.length; i++) {
            if (subject_classes[i] == 'gene') {
                for (let j = 0; j < observation_number; j++) {
                    const gene = observationArray[j * rows + i];
                    if (gene.length > 32) {
                        return 'gene symbol "' + gene + '" is longer than 32 for observation ' + (j + 1) + " at row " + (i + 1);
                    }
                }
            }
        }

        return '';
    };

    const UPLOAD_SIZE_LIMIT = 10485760; // 10 MB

    const uploadZip = function (uploadButton) {
        const filelList = $(uploadButton).prop('files');
        if (filelList != null && filelList.length > 0) {
            const file = filelList[0];

            if (file.size > UPLOAD_SIZE_LIMIT) {
                showAlertMessage('Size of file ' + file.name + ' is ' + file.size + ' bytes and over the allowed limit, so it is ignored.');
                $(this).val("");
            } else if (!(file.name.toLowerCase().endsWith(".zip"))) {
                showAlertMessage('You can only upload a ZIP file (containing a spreadsheet etc) to be validated, so ' + file.name + ' is ignored.');
                $(this).val("");
            } else {
                const reader = new FileReader();
                reader.addEventListener("load", function () {

                    $('#validation-progress').modal('show');
                    $.ajax({
                        async: true,
                        url: "upload/zip",
                        type: "POST",
                        data: {
                            filename: file.name,
                            filecontent: reader.result,
                            centerId: centerId
                        },
                        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                        success: function (response) {
                            $('#validation-progress').modal('hide');
                            response.templateId = "unzipped/" + file.name; // name of subdirectory where reported is saved
                            (new ValidationReportView({
                                model: response,
                            })).render();
                        },
                        error: function (response, status) {
                            $('#validation-progress').modal('hide');
                            console.log(response);
                            showAlertMessage(status + '<br>Status ' + response.status + " " + response.statusText);
                        }
                    });

                }, false);
                reader.readAsDataURL(file);
                console.log("start reading " + file.name);
            }
        } else {
            console.log("filelList is empty");
        }
    };

    /* this is the last step on the 'Submission Data' page after possible background reading is done */
    const update_after_all_data_ready = function (triggeringButton) {

        const validate = function () {
            let message = validate_column_tags(subjects, 'subject column tag');
            if (message != null && message.length > 0) {
                return message;
            }

            message = validate_column_tags(evidences, 'evidence column tag');
            if (message != null && message.length > 0) {
                return message;
            }
            message = validate_description(subjectDescriptions, 'subject description');
            if (message != null && message.length > 0) {
                return message;
            }
            message = validate_description(evidenceDescriptions, 'evidence description');
            if (message != null && message.length > 0) {
                return message;
            }
            return '';
        };

        const subjects = getArray('#template-table-subject input.subject-columntag');
        const subjectDescriptions = getArray('#template-table-subject input.subject-descriptions');
        const evidences = getArray('#template-table-evidence input.evidence-columntag');
        const evidenceDescriptions = getArray('#template-table-evidence input.evidence-descriptions');

        const validation_message = validate(); // some arrays are converted to string after validation
        if (validation_message != null && validation_message.length > 0) {
            showAlertMessage("<ul>" + validation_message + "</ul>");
            saveSuccess = false;
            return;
        }

        const subject_classes = getArray('#template-table-subject select.subject-classes');
        const observation_number = $(".observation-header").length / 2;
        const observation_validation_message = validate_observation(subject_classes, observation_number);
        if (observation_validation_message != null && observation_validation_message.length > 0) {
            showAlertMessage("<ul>" + observation_validation_message + "</ul>");
            saveSuccess = false;
            return;
        }

        currentModel.set({
            subjectColumns: subjects,
            subjectClasses: subject_classes,
            subjectRoles: getArray('#template-table-subject select.subject-roles'),
            subjectDescriptions: subjectDescriptions,
            evidenceColumns: evidences,
            evidenceTypes: getArray('#template-table-evidence select.evidence-types'),
            valueTypes: getArray('#template-table-evidence select.value-types'),
            evidenceDescriptions: evidenceDescriptions,
            observationNumber: observation_number,
            observations: observationArray,
        });
        updateTemplate(triggeringButton);
    };

    // update model from the observation summary page
    const update_model_from_summary_page = function (triggeringButton) {
        const summary = $("#template-obs-summary").val();
        if (summary.length > 1024) {
            showAlertMessage("<ul>The summary that you entered has " + summary.length + " characters. 1024 characters is the designed limit of this field. Please modify it before trying to save again.</ul>");
            saveSuccess = false;
            return;
        }
        const ecos = $("#common-ecoterms").DataTable().rows({
            selected: true
        }).data();
        let ecocodes = '';
        for (var i = 0; i < ecos.length; i++) {
            if (i > 0) ecocodes += '|';
            ecocodes += ecos[i][1].split(' ')[0];
        }
        // get the 'open' entries of ECO codes
        const x = $("#eco-code-open-entries").val();
        const matches = x.matchAll(/ECO(:|_)\d{7}/g);
        for (const match of matches) {
            let eco_code = match[0];
            if (match[1] == '_') eco_code = match[0].replace('_', ':');
            if (ecocodes.length > 0) ecocodes += '|';
            ecocodes += eco_code;
        }

        currentModel.set({
            summary: summary,
            ecocodes: ecocodes,
        });

        triggeringButton.attr("disabled", "disabled");
        $.ajax({
            url: "template/update-summary",
            async: false,
            type: "POST",
            data: jQuery.param(currentModel.toJSON()),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (data) {
                console.log("return value: " + data);
                triggeringButton.removeAttr("disabled");
                refreshTemplateList();
                currentModel = templateModels[currentModel.id]; // the values are not changed but it is a different object after refreshing
                updatePreview();
            },
            error: function (response, status) {
                triggeringButton.removeAttr("disabled");
                // response.responseText is an HTML page
                console.log(status + ": " + response.responseText);
                showInvalidMessage("The template data was NOT saved to the server for some unexpected error. " +
                    "Please contact the administrator of this application to help finding out the specific cause and fixing it. " +
                    "Sorry for the inconvenience.");
            }
        });
    };

    const popupLargeTextfield = function () {
        const trigger = $(this);
        $('#popup-textarea-modal').on('hidden.bs.modal', function (e) {
            trigger.prop('disabled', false);
        });
        trigger.prop('disabled', true);

        $('#popup-textarea-modal').on('shown.bs.modal', function () {
            $("#temporary-text").focus();
        });

        $("#invoker-id").text($(this).attr('id'));
        $("#temporary-text").val($(this).val());
        $("#popup-textarea-modal").modal('show');
    };

    const closeLargeTextfield = function () {
        const invoker_id = $("#invoker-id").text();
        $('#' + invoker_id).val($("#temporary-text").val());
    };

    const escapeQuote = function (s) {
        if (s === undefined || s == null) return "";
        return s.replace(/^[\"]+|[\"]+$/g, "").replace(/\"/g, "&quot;");
    };

    const showPage = function (page_name, menu_item) {
        $("#center-select-page").fadeOut();
        $("#submission-list-page").fadeOut();
        $("#description-page").fadeOut();
        $("#submission-data-page").fadeOut();
        $("#observation-summary-page").fadeOut();
        $("#preview-page").fadeOut();
        $("#upload-view").fadeOut();
        $(page_name).slideDown();
        // set current page indicator
        $("#menu_description").removeClass('current-page');
        $("#menu_data").removeClass('current-page');
        $("#menu_summary").removeClass('current-page');
        $("#menu_preview").removeClass('current-page');
        $(menu_item).addClass("current-page"); // if menu_item is null, it is OK
    };

    const showTemplateMenu = function () {
        $("#menu_description").show();
        $("#menu_data").show();
        $("#menu_summary").show();
        $("#menu_preview").show();
    };

    const hideTemplateMenu = function () {
        $("#menu_description").hide();
        $("#menu_data").hide();
        $("#menu_summary").hide();
        $("#menu_preview").hide();
    };

    const deleteTemplate = function (tobeDeleted) {
        $("#confirmed-delete").unbind('click').click(function () {
            $(this).attr("disabled", "disabled");
            $.ajax({
                async: false,
                url: "template/delete",
                type: "POST",
                data: jQuery.param({
                    templateId: tobeDeleted,
                }),
                contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                success: function (response) {
                    console.log(response);
                    $("#template-table-row-" + tobeDeleted).remove();
                }
            });
            $(this).removeAttr("disabled");
        });
        $('#confirmation-message').text("Are you sure you want to delete this submission template?");
        $("#confirmation-modal").modal('show');
    };

    const getArray = function (searchTag) {
        const s = [];
        $(searchTag).each(function (i, row) {
            s.push($(row).val().trim());
        });
        return s;
    };

    // on 'Submission Data' page
    const disable_saving_buttons = function () {
        $("#save-template-submission-data").attr("disabled", "disabled");
        $("#apply-template-submission-data").attr("disabled", "disabled");
    };

    // on 'Submission Data' page
    const enable_saving_buttons = function () {
        $("#save-template-submission-data").removeAttr("disabled");
        $("#apply-template-submission-data").removeAttr("disabled");
    };

    const update_model_from_submission_data_page = function () {
        disable_saving_buttons();
        saveSuccess = true;
        getObservations(); // this may have started multiple threads reading the evidence files
        if (!saveSuccess) {
            saveSuccess = true; // reset. this does not really mean 'success'
            enable_saving_buttons();
            return;
        }

        /* this function's purpose is to keep checking the threads started by getObservations()
        if finished, it continues to call update_after_all_data_ready(...)
        if not, wait 1 second and check again. */
        const attempt_to_proceed_updating = function (triggeringButton) {
            if (file_number === finished_file_number) {
                update_after_all_data_ready(triggeringButton);
                enable_saving_buttons(); //re-enable the save button when all reading is done
                if (triggeringButton.hasClass("proceed-to-next-page")) {
                    if (saveSuccess) {
                        populateTagList();
                        showPage("#observation-summary-page", "#menu_summary");
                    } else {
                        saveSuccess = true; // reset the flag
                    }
                }
                return;
            }
            setTimeout(attempt_to_proceed_updating, 1000, triggeringButton);
        };

        attempt_to_proceed_updating($(this));
    };

    const updateTemplate = function (triggeringButton) {

        triggeringButton.attr("disabled", "disabled");
        $.ajax({
            url: "template/update",
            async: false,
            type: "POST",
            data: jQuery.param(currentModel.toJSON()),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (data) {
                console.log("return value: " + data);
                triggeringButton.removeAttr("disabled");
                refreshTemplateList();
                currentModel = templateModels[currentModel.id]; // the values are not changed but it is a different object after refreshing
                updatePreview();
            },
            error: function (response, status) {
                triggeringButton.removeAttr("disabled");
                // response.responseText is an HTML page
                console.log(status + ": " + response.responseText);
                showInvalidMessage("The template data was NOT saved to the server for some unexpected error. " +
                    "Please contact the administrator of this application to help finding out the specific cause and fixing it. " +
                    "Sorry for the inconvenience.");
            }
        });
    };

    const saveNewTemplate = function (async) {
        if (centerId == 0) {
            console.log('error: unexpected centerId==0');
            return;
        }
        const submissionName = $("#template-name").val();

        const firstName = $("#first-name").val();
        const lastName = $("#last-name").val();

        if (firstName.length == 0 || lastName.length == 0 ||
            submissionName.length == 0) {
            console.log("not saved due to incomplete information");
            $("#save-name-description").removeAttr("disabled");
            showAlertMessage("new template cannot be created withnot required information: first name, last name, and a submission name");
            return false; // error control
        }

        let result = false;
        $.ajax({
            async: async,
            url: "template/create",
            type: "POST",
            data: jQuery.param({
                centerId: centerId,
                displayName: submissionName,
                firstName: firstName,
                lastName: lastName,
                email: $("#email").val(),
                phone: $("#phone").val(),
                description: $("#template-submission-desc").val(),
                project: $("#template-project-title").val(),
                tier: $("#template-tier").val(),
                isStory: $("#template-is-story").is(':checked'),
                storyTitle: $('#story-title').val(),
                piName: $('#pi-name').val(),
            }),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (resultId) {
                $("#save-name-description").removeAttr("disabled");
                result = true;
                $("span#submission-name").text(submissionName);
                showTemplateMenu();
                refreshTemplateList();
                currentModel = templateModels[resultId];
            },
            error: function (response, status) {
                $("#save-name-description").removeAttr("disabled");
                console.log(status + ": " + response.responseText);
                showInvalidMessage("The new template was NOT created for some unexpected error. " +
                    "Please contact the administrator of this application to help finding out the specific cause and fixing it. " +
                    "Sorry for the inconvenience.");
            }
        });
        if (async ||result)
            return true;
        else
            return false;
    };

    const clone = function (templateId) {
        if (centerId == 0) {
            console.log('error: unexpected centerId==0');
            return;
        }
        $("#template-table-row-" + templateId).attr("disabled", "disabled");
        $.ajax({
            url: "template/clone",
            type: "POST",
            data: jQuery.param({
                centerId: centerId,
                templateId: templateId
            }),
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function (resultId) {
                $("#template-table-row-" + templateId).removeAttr("disabled");
                currentModel = null;
                showTemplateMenu();
                refreshTemplateList();
                console.log('clone succeeded ' + templateId + ' -> ' + resultId);
            },
            error: function (response, status) {
                console.log(status + ": " + response.responseText);
                showInvalidMessage("The template was NOT cloned successfully for some unexpected error. " +
                    "Please contact the administrator of this application to help finding out the specific cause and fixing it. " +
                    "Sorry for the inconvenience.");
                $("#template-table-row-" + tmpltModel.id).removeAttr("disabled");
            }
        });
    };

    const addNewSubject = function (tag) {
        (new TemplateSubjectDataRowView({
            model: {
                columnTagId: $("#template-table-subject tr").length - 1,
                columnTag: tag,
                subjectClass: null,
                subjectRole: null,
                subjectDescription: null,
                observationNumber: $(".observation-header").length / 2,
                observations: []
            },
            el: $("#template-table-subject")
        })).render();
    };

    const addNewEvidence = function (tag) {
        (new TemplateEvidenceDataRowView({
            model: {
                columnTagId: $("#template-table-evidence tr").length - 1,
                columnTag: tag,
                evidenceType: null,
                valueType: null,
                evidenceDescription: null,
                observationNumber: $(".observation-header").length / 2,
                observations: []
            },
            el: $("#template-table-evidence")
        })).render();
    };

    const populateOneTemplate = function () {
        if (currentModel == null || currentModel === undefined) {
            /* case of new template */
            currentModel = new SubmissionTemplate();
            currentModel.set({
                piName: defaultPis[centerId]
            });
        }
        $("#template-id").val(currentModel.id); /* used by download form only */
        const rowModel = currentModel.toJSON();

        $("span#submission-name").text(rowModel.displayName);

        (new SubmitterInformationView({
            model: currentModel,
            el: $("#submitter-information")
        })).render();
        (new TemplateDescriptionView({
            model: currentModel,
            el: $("#template-description")
        })).render();

        $("#template-table-subject > .template-data-row").remove();
        const subjectColumns = rowModel.subjectColumns; // this is an array of strings
        const subjectClasses = rowModel.subjectClasses; // this is an array of strings
        const observations = rowModel.observations;
        const observationNumber = rowModel.observationNumber;
        const evidenceColumns = rowModel.evidenceColumns;

        // make headers for observation part
        $("th.observation-header").remove();
        const remove_column = function () {
            const c = $('#template-table tr#subject-header').find('th').index($(this).parent());
            $('#template-table tr').find('td:eq(' + c + '),th:eq(' + c + ')').remove();
        };
        for (let column = 1; column <= observationNumber; column++) {
            const deleteButton = "delete-column-" + column;
            $("#template-table tr#subject-header").append("<th class=observation-header>Observation " + column + "<br>(<button class='btn btn-link' id='" + deleteButton + "'>delete</button>)</th>");
            $("#template-table tr#evidence-header").append("<th class=observation-header>Observation " + column + "</th>");
            $("#" + deleteButton).click(remove_column);
        }
        if (Array.isArray(rowModel.subjectDescriptions) && rowModel.subjectColumns.length == 1) {
            rowModel.subjectDescriptions[0] = rowModel.subjectDescriptions.toString(); // prevent the single element containing commas being treated as an array
        }

        const subjectRows = subjectColumns.length;
        const evidenceRows = evidenceColumns.length;
        const totalRows = subjectRows + evidenceRows;
        for (let i = 0; i < subjectColumns.length; i++) {
            const observationsPerSubject = new Array(observationNumber);
            for (let column = 0; column < observationNumber; column++) {
                observationsPerSubject[column] = observations[totalRows * column + i];
            }

            (new TemplateSubjectDataRowView({
                model: {
                    columnTagId: i,
                    columnTag: subjectColumns[i],
                    subjectClass: subjectClasses[i],
                    subjectRole: rowModel.subjectRoles[i],
                    subjectDescription: rowModel.subjectDescriptions[i],
                    totalRows: totalRows,
                    row: i,
                    observationNumber: observationNumber,
                    observations: observationsPerSubject
                },
                el: $("#template-table-subject")
            })).render();
        }
        if (subjectRows == 0) addNewSubject('subject 1');

        $("#template-table-evidence > .template-data-row").remove();
        const evidenceTypes = rowModel.evidenceTypes;
        const valueTypes = rowModel.valueTypes;
        const evidenceDescriptions = rowModel.evidenceDescriptions;
        if (Array.isArray(evidenceDescriptions) && evidenceColumns.length == 1) {
            evidenceDescriptions[0] = evidenceDescriptions.toString(); // prevent the single element containing commas being treated as an array
        }
        for (let i = 0; i < evidenceColumns.length; i++) {
            const observationsPerEvidence = new Array(observationNumber);
            for (column = 0; column < observationNumber; column++) {
                observationsPerEvidence[column] = observations[totalRows * column + i + subjectRows];
            }
            (new TemplateEvidenceDataRowView({
                model: {
                    columnTagId: i,
                    columnTag: evidenceColumns[i],
                    evidenceType: evidenceTypes[i],
                    valueType: valueTypes[i],
                    evidenceDescription: evidenceDescriptions[i],
                    totalRows: totalRows,
                    row: i + subjectRows,
                    observationNumber: observationNumber,
                    observations: observationsPerEvidence
                },
                el: $("#template-table-evidence")
            })).render();
        }
        if (evidenceColumns.length == 0) addNewEvidence('evidence 1');

        $("#template-obs-summary").val(rowModel.summary);
        const ecos = $("#common-ecoterms").DataTable().rows();
        ecos.rows().deselect();
        $("#eco-code-open-entries").val('');
        if (rowModel.ecoCodes != null) {
            const x = rowModel.ecoCodes.split('|');
            const common_codes = [];
            const ecos_d = ecos.data();
            for (var i = 0; i < ecos.count(); i++) {
                const y = ecos_d[i][1].split(' ')[0];
                if (x.includes(y)) {
                    ecos.row(i).select();
                    common_codes.push(y)
                }
            }
            const open_codes = x.filter(function (value) {
                return !common_codes.includes(value);
            });
            $("#eco-code-open-entries").val(open_codes);
        }
        updatePreview();
    };

    const updatePreview = function () { // this should be called when the template data (model) changes
        $("#preview-select").empty();
        $("#preview-page [id^=observation-preview-]").remove();
        const observationNumber = currentModel.get('observationNumber');
        for (let i = 0; i < observationNumber; i++) {
            (new ObservationOptionView({
                model: {
                    observation_id: i
                },
                el: $("#preview-select")
            })).render();
        }

        const observationPreviewView = new ObservationPreviewView({
            el: $("#preview-container")
        });
        if (observationNumber > 0) {
            observationPreviewView.model = currentModel.getPreviewModel(0);
            observationPreviewView.render();
        }

        $("#preview-select").unbind('change').change(function () {
            const selected = parseInt($(this).val());
            if (selected < 0 || selected >= observationNumber) {
                console.log('error in preview selected ' + selected);
                return;
            }
            observationPreviewView.model = currentModel.getPreviewModel(selected);
            observationPreviewView.render();
        });
    };

    const refreshTemplateList = function () {
        if (centerId == 0) {
            console.log('error: unexpected centerId==0');
            return;
        }
        templateModels = {};
        const storedTemplates = new StoredTemplates({
            centerId: centerId
        });
        $("#existing-template-table > .stored-template-row").remove();
        storedTemplates.fetch({
            async: false,
            success: function () {
                _.each(storedTemplates.models, function (oneTemplateModel) {
                    if (oneTemplateModel.get("piName") == null) {
                        oneTemplateModel.set({
                            piName: defaultPis[centerId]
                        });
                    }
                    templateModels[oneTemplateModel.id] = oneTemplateModel;

                    (new ExistingTemplateView({
                        model: oneTemplateModel,
                        el: $("#existing-template-table")
                    })).render();
                });
            }
        });
    };

    const populateTagList = function () {
        $("#column-tag-list").empty();
        $('#template-table').find('.subject-columntag').each(function (index, item) {
            (new ColumnTagView({
                model: {
                    id: index,
                    tag: $(item).val()
                },
                el: $("#column-tag-list")
            })).render();
        });
        $('#template-table').find('.evidence-columntag').each(function (index, item) {
            (new ColumnTagView({
                model: {
                    id: index,
                    tag: $(item).val()
                },
                el: $("#column-tag-list")
            })).render();
        });
        $(".helper-tag").click(function () {
            const input = $("#template-obs-summary");
            input.val(input.val() + "<" + $(this).text() + ">");
        });
    };

    const showAlertMessage = function (message) {
        $("#alertMessage").html(message);
        $("#alertMessage").css('color', '#5a5a5a');
        $("#alert-message-modal").modal('show');
    };

    const showInvalidMessage = function (message) {
        $("#alertMessage").text(message);
        $("#alertMessage").css('color', 'red');
        $("#alert-message-modal").modal('show');
    };

    const getObservations = function () {
        const columns = $(".observation-header").length / 2;
        const rows = $("#template-table tr").length - 4; // two rows for subject/evidence headers, two rows for the headers of each section
        observationArray = new Array(rows * columns);

        file_number = 0;
        finished_file_number = 0;

        $("#template-table tr.template-data-row").each(function (i, row) {
            const row_id = $(row).attr('id');
            $(row).find("[id^=observation]").each(function (j, c) {
                const UPLOAD_TYPES_ALLOWED = ['image/png', 'image/jpeg', 'application/pdf', 'application/msword',
                    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                ];

                let valuetype = ""; // only applicable for evidence, not for subject
                if (row_id.startsWith("template-evidence-row")) {
                    const cell_id = $(c).attr('id');
                    const columntag = cell_id.substring(cell_id.indexOf('-', 12) + 1); // skip the first dash
                    valuetype = $("#value-type-" + columntag).val();
                }

                const value = $(c).val();
                // check the lengths
                if (valuetype === 'url' && value.length > 2048) {
                    showAlertMessage("URL is too long (longer than 2048)");
                    saveSuccess = false;
                    return;
                } else if (valuetype === "file" && value.length > 1024) { // file name including fake path
                    showAlertMessage("file name is too long (longer than 1024)");
                    saveSuccess = false;
                    return;
                }

                if (valuetype != 'file') {
                    observationArray[j * rows + i] = value;
                } else { // if the value type is 'file', a reading thread would be started
                    const p = $(c).prop('files');
                    if (p != null && p.length > 0) {
                        const file = p[0];

                        const isSifFile = file.type == "" && file.name.toLowerCase().endsWith(".sif");
                        if (file.size > UPLOAD_SIZE_LIMIT) {
                            showAlertMessage('Size of file ' + file.name + ' is ' + file.size + ' bytes and over the allowed limit, so it is ignored.');
                            observationArray[j * rows + i] = "";
                            $(c).val("");
                        } else if (!(UPLOAD_TYPES_ALLOWED.includes(file.type) || isSifFile)) {
                            showAlertMessage('Type of file ' + file.name + ' is "' + file.type + '" and not allowed to be uploaded, so it is ignored.');
                            observationArray[j * rows + i] = "";
                            $(c).val("");
                        } else {
                            const reader = new FileReader();
                            reader.addEventListener("load", function () {
                                const filecontent = reader.result.replace("base64,", "base64:"); // comma breaks later processing
                                observationArray[j * rows + i] = file.name + "::" + filecontent;
                                finished_file_number++;
                            }, false);
                            reader.readAsDataURL(file);

                            file_number++;
                        }
                    } else {
                        observationArray[j * rows + i] = "";
                    }
                }
            });
        });
        // when this function returns here, there are possibly multiple background threads started by this function that are reading files
    };

    return TemplateHelperView;

})(window.jQuery);