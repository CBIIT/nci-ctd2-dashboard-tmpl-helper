<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%
    WebApplicationContext context = WebApplicationContextUtils
            .getWebApplicationContext(application);
    String dataURL = (String) context.getBean("dataURL");
    String submissionBuilderVersion = (String) context.getBean("submissionBuilderVersion");
%><!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
  <head>
    <!-- X-UA-Compatible meta tag to disable IE compatibility view must always be first -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>CTD² Dashboard</title>
    <meta name="description" content="" />
    <meta name="author" content="" />

    <link rel="shortcut icon" href="img/favicon.ico" type="image/vnd.microsoft.icon" />
    <link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" />
    <link rel="stylesheet" href="css/jquery.fancybox.min.css" type="text/css" media="screen" />
    <link rel="stylesheet" href="css/ctd2.css?ts=2019" type="text/css" />

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="shortcut icon" href="img/favicon.png" />
  </head>

  <body>
    <!-- NAVBAR
    ================================================== -->
    <script src="js/jquery-3.3.1.min.js"></script>
    <div class="navbar-wrapper">
      <!-- Wrap the .navbar in .container to center it within the absolutely positioned parent. -->
      <div class="container">

        <div class="navbar">
          <div class="navbar-inner">
            <div class="nav-collapse collapse show">
              <ul id="nav" class="nav">
                <li><a id="navlink-dashboard" class="navlink" href="/dashboard/#">CTD<sup>2</sup> Dashboard</a></li>
                <li><a id="navlink-centers" class="navlink" href="/dashboard/#centers">Centers</a></li>
                <li class="dropdown">
                      <a class="dropdown-toggle navlink" href="/dashboard/#" data-toggle="dropdown">Resources <b class="caret"></b></a>
                      <ul class="dropdown-menu">
                          <li><a target="_blank" href="https://ocg.cancer.gov/programs/ctd2">CTD<sup>2</sup> Home Page</a></li>
                          <li><a target="_blank" href="https://ocg.cancer.gov/programs/ctd2/publications">Publications</a></li>
                          <li><a target="_blank" href="https://ocg.cancer.gov/programs/ctd2/data-portal">Data Portal - Downloads</a></li>
                          <li><a target="_blank" href="https://ocg.cancer.gov/programs/ctd2/analytical-tools">Analytical Tools</a></li>
                          <li><a target="_blank" href="https://ocg.cancer.gov/programs/ctd2/supported-reagents">Supported Reagents</a></li>
                          <li class="dropdown-submenu"><a tabindex="-1" href="/dashboard/#">Outside Resources</a>
                                <ul class="dropdown-menu">
                                    <li><a target="_blank" href="http://www.lincsproject.org/">LINCS</a></li>
                                </ul>
                           </li>
                      </ul>
                  </li>
                  <li class="dropdown">
                      <a id="navlink-browse" class="dropdown-toggle navlink" href="/dashboard/#" data-toggle="dropdown">Browse <b class="caret"></b></a>
                      <ul id="dropdown-menu-browse" class="dropdown-menu">
                          <li><a href="/dashboard/#stories">Stories</a></li>
                          <li><a href="/dashboard/#explore/target/Biomarker,Target">Genes (Biomarkers, Targets, etc.)</a></li>
                          <li><a href="/dashboard/#explore/compound/Perturbagen,Candidate Drug">Compounds and Perturbagens</a></li>
                          <li><a href="/dashboard/#explore/context/Disease">Disease Contexts</a></li>
                      </ul>
                  </li>
                  <li class="dropdown">
                      <a id="navlink-genecart" class="dropdown-toggle navlink" href="/dashboard/#" data-toggle="dropdown">Gene Cart <b class="caret"></b></a>
                      <ul id="dropdown-menu-genecart" class="dropdown-menu">
                          <li><a href="/dashboard/#genes">Go To Cart</a></li>
                          <li><a href="/dashboard/#gene-cart-help">Help</a></li>
                      </ul>
                  </li>
              </ul>
              <ul class="nav pull-right">
                  <form class="form-search" id="omnisearch">
                      <div class="input-append">
                          <input type="text" id="omni-input" class="search-query" title="Search" placeholder="e.g. CTNNB1 or dasatinib" aria-label="search">
                          <button type="submit" class="btn search-button">Search</button>
                          <span class="d-none" id="search-help-content">
                              <p>Please enter the keyword you would like to search on the website.</p>
                              <strong>Examples:</strong>
                              <ul>
                                <li><em>Gene: </em> <a href="/dashboard/#search/CTNNB1">CTNNB1</a></li>
                                <li><em>Gene: </em> <a href="/dashboard/#search/YAP*">YAP*</a></li>
                                <li><em>Compound: </em> <a href="/dashboard/#search/dasatinib">dasatinib</a></li>
                                <li><em>Cell Sample: </em> <a href="/dashboard/#search/OVCAR8">OVCAR8</a></li>
                                <li><em>Multiple: </em> <a href="/dashboard/#search/dexamethasone AKT1">dexamethasone AKT1</a></li>
                              </ul>
                              <br>
                          </span>
                      </div>
                  </form>
              </ul>
            </div><!--/.nav-collapse -->
          </div><!-- /.navbar-inner -->
        </div><!-- /.navbar -->

      </div> <!-- /.container -->
    </div><!-- /.navbar-wrapper -->


    <!-- all the backbone magic will happen here, right in this div -->
    <div id="main-container"></div>

    <div class="container footer-container">
        <!-- FOOTER -->
        <footer>
            <div style="font-size:14px; font-weight:bold; margin-bottom:10px;">
                Submission Builder <%=submissionBuilderVersion%>
                <a href="#attribution" data-toggle="collapse">attributions</a>
            </div>
            <div id="attribution" class="collapse">
            <div style="font-size:14px; margin-bottom:10px;">
               Data users must acknowledge and cite the manuscript 
               <a href= "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5569694/"> Aksoy, Dančík, Smith et al.,</a> Database 2017;1-10
               and provide the URL 
               <a href= "https://ctd2-dashboard.nci.nih.gov/dashboard/"> "https://ctd2-dashboard.nci.nih.gov/dashboard/"</a>.
            </div>

                  
            <div style="font-size:14px; margin-bottom:10px;">
                As the CTD<sup>2</sup> Network continues to refine the Dashboard, input from the research community is highly valued to help improve usability.
                Please send your feedback and comments to 
                <a href="mailto:ocg@mail.nih.gov?subject=CTD2 Dashboard Feedback">ocg@mail.nih.gov</a>.
            </div>

            <div style="font-size:14px; margin-bottom:10px;">
                <a href="http://cancer.gov"><img src="img/logos/footer_logo_nci.jpg" alt="NCI logo" title="NCI logo"></a><a href="http://www.dhhs.gov/"><img src="img/logos/footer_logo_hhs.jpg" title="HHS logo" alt="HHS logo"></a><a href="http://www.nih.gov/"><img src="img/logos/footer_logo_nih.jpg" title="NIH logo" alt="NIH logo"></a><a href="http://www.firstgov.gov/"><img src="img/logos/footer_logo_firstgov.jpg" title="First Gov logo" alt="First Gov logo"></a>
            </div>
            <div style="font-size:14px; margin-bottom:10px;">
                <a class="help-navigate" href="#help-navigate">Glossary</a> &middot;
                <a href="http://www.cancer.gov/global/web/policies" target="_blank">Policies</a> &middot;
                <a href="http://www.cancer.gov/global/web/policies/accessibility" target="_blank">Accessibility</a> &middot;
                <a href="http://www.cancer.gov/global/web/policies/foia" target="_blank">FOIA</a>
            </div>
            </div>
        </footer>
    </div>
     
    <div class="modal hide fade" id="alert-message-modal">  <!-- a hidden div for showing alert message -->
      <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-body" >
            <br><medium id="alertMessage"></medium>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal">Close</button>
        </div>
      </div>
      </div>
    </div>

    <div class="modal hide fade" id="popup-textarea-modal">
      <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-body" >
            <textarea id="temporary-text" style='width:95%' rows='10' cols='100'></textarea>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal" id="close-tempoary-text">Close</button>
        </div>
        <span id='invoker-id' style='display:none'></span>
      </div>
      </div>
    </div>

    <div class="modal fade" tabindex="-1" role="dialog" id="confirmation-modal">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Delete Confirmation</h4>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                </div>
                <div class="modal-body">
                    <p id="confirmation-message">Are you sure you want to delete this?</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" id="confirmed-delete">Yes, delete it.</button>
                </div>
            </div>
        </div>
    </div>

    <!-- these are the templates -->
    <script type="text/template" id="observation-tmpl">
        <div class="container common-container" id="observation-container">

            <div class="row">
                <div class="col-10">
                    <h2>Observation <small>(Tier {{submission.observationTemplate.tier}})</small></h2>
                    <blockquote>
                        <p id="observation-summary"></p>
                    </blockquote>

                    <table id="observed-subjects-grid" class="table table-bordered table-striped subjects">
                        <thead>
                        <tr>
                            <th width="60">&nbsp;&nbsp;&nbsp;&nbsp;</th>
                            <th>Name</th>
                            <th>Class</th>
                            <th>Role</th>
                            <th>Description</th>
                        </tr>
                        </thead>
                        <tbody>
                        <!-- here will go the rows -->
                        </tbody>
                    </table>

                </div>
                <div class="span2">
                    <a href="#/center/{{submission.observationTemplate.submissionCenter.id}}"><img src="img/{{submission.observationTemplate.submissionCenter.displayName}}.png" class="img-polaroid" width="120" alt="{{submission.observationTemplate.submissionCenter.displayName}}"></a>
                    <br><br>
                    <img src="img/observation.png" alt="Observation" class="img-polaroid" width=120 height=120><br>
                </div>
            </div>


            <h3>Submission <small>(<a href="#" id="small-show-sub-details">show details</a><a href="#" id="small-hide-sub-details" class="hide">hide details</a>)</small></h3>
            <div id="obs-submission-details" class="hide">
                <table id="obs-submission-details-grid" class="table table-bordered table-striped">
                    <tr>
                        <th>Project</th>
                        <td>{{submission.observationTemplate.project}}</td>
                    </tr>
                    <tr>
                        <th>Description</th>
                        <td>
                            {{submission.observationTemplate.description}}
                            <small>(<a href="#submission/{{submission.id}}">details &raquo;</a>)</small>
                        </td>
                    </tr>
                    <tr id="obs-submission-summary">
                        <th>Summary</th>
                        <td>{{submission.observationTemplate.submissionDescription}}</td>
                    </tr>
                    <tr>
                        <th>Date</th>
                        <td>{{submission.submissionDate}}</td>
                    </tr>
                    <tr>
                        <th>Source Data</th>
                        <td><a href="<%=dataURL%>submissions/{{submission.displayName}}.zip" class=no-preview>download</a></td>
                    </tr>


                </table>
            </div>


            <h3>Evidence</h3>
            <table id="observed-evidences-grid" class="table table-bordered table-striped evidences">
                <thead>
                <tr>
                    <th>&nbsp;&nbsp;</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th width="150">Details</th>
                </tr>
                </thead>
                <tbody>
                <!-- here will go the rows -->
                </tbody>
            </table>

        </div>
    </script>

    <script type="text/template" id="summary-subject-replacement-tmpl">
        <a class="summary-replacement" href="#/subject/{{id}}">{{displayName}}</a>
    </script>

    <script type="text/template" id="summary-evidence-replacement-tmpl">
        <strong class="summary-replacement">{{displayName}}</strong>
    </script>

    <script type="text/template" id="observedevidence-row-tmpl">
        <tr>
            <td>&nbsp;&nbsp;</td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>{{displayName}}</td>
        </tr>
    </script>

    <script type="text/template" id="observedfileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>(
                <a href="<%=dataURL%>{{evidence.filePath}}" target="_blank" title="Download file ({{evidence.mimeType}})" class="desc-tooltip  no-preview" title="Download File">
                    download file
                </a>
            )</td>
        </tr>
    </script>

    <script type="text/template" id="observedhtmlfileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>(
                <a href="<%=dataURL%>{{evidence.filePath}}" title="View file ({{evidence.mimeType}})" class="desc-tooltip html-story-link" title="Download File">
                    view
                </a>
                )</td>
        </tr>
    </script>


    <script type="text/template" id="observedpdffileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>(
                <a href="<%=dataURL%>{{evidence.filePath}}" target="_blank" title="{{observedEvidenceRole.displayText}}" class="desc-tooltip pdf-file-link">
                    view PDF
                </a>
                )</td>
        </tr>
    </script>

    <script type="text/template" id="observedgctfileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>
                <div class="dropdown">
                    ( <a class="dropdown-toggle" data-toggle="dropdown" href="#">view file <b class="caret"></b></a> )
                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                        <li>
                            <a href="http://www.broadinstitute.org/cancer/software/GENE-E/dynamic.php?data=<%=dataURL%>{{evidence.filePath}}" target="_blank" title="open in GENE-E (Java Web-start)" class="desc-tooltip">
                                open with GENE-E
                            </a>
                        </li>
                        <li>
                            <a href="<%=dataURL%>{{evidence.filePath}}" class="desc-tooltip" target="_blank" title="type: ({{evidence.mimeType}})">view in browser</a>
                        </li>

                    </ul>
                </div>
            </td>
        </tr>
    </script>

    <script type="text/template" id="observedsiffileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>
                <div class="dropdown">
                    ( <a class="dropdown-toggle" data-toggle="dropdown" href="#">view file <b class="caret"></b></a> )
                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                        <li>
                            <a href="<%=dataURL%>{{evidence.filePath}}" class="desc-tooltip" target="_blank" title="type: ({{evidence.mimeType}})">view in browser</a>
                        </li>
                    </ul>
                </div>
            </td>
        </tr>
    </script>

    <script type="text/template" id="observedimageevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>
                <div class="image-evidence-wrapper">
                    <a href="<%=dataURL%>{{evidence.filePath}}" target="_blank" title="{{observedEvidenceRole.displayText}}" rel="evidence-images" class="evidence-images">
                        <img src="<%=dataURL%>{{evidence.filePath}}" class="img-polaroid img-evidence" height="140" title="File" alt="File">
                    </a>
                </div>
            </td>
        </tr>
    </script>


    <script type="text/template" id="observedlabelevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td><div class="labelevidence expandable">{{displayName}}</div></td>
        </tr>
    </script>

    <script type="text/template" id="observedurlevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>
                (<a href="{{evidence.url.replace(/^\//, '')}}" target="_blank" class="desc-tooltip no-preview" title="Open link in a new window">
                    open link
                </a>)
            </td>
        </tr>
    </script>

    <script type="text/template" id="observeddatanumericevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td><span class="numeric-value">{{evidence.numericValue}}</span> <em>{{evidence.unit}}</em></td>
        </tr>
    </script>

    <script type="text/template" id="observeddatanumericevidence-val-tmpl">
        {{firstPart}} &times; 10<sup>{{secondPart}}</sup>
    </script>

    <script type="text/template" id="observedsubject-summary-row-tmpl">
        <tr>
            <td id="subject-image-{{id}}"></td>
            <td>
                <a href="#/subject/{{subject.id}}">
                    {{subject.displayName}}
                </a>
            </td>
            <td>{{subject.type}}</td>
            <td>{{observedSubjectRole.subjectRole.displayName}}</td>
            <td>{{observedSubjectRole.displayText}}</td>
        </tr>
    </script>
    
    <script type="text/template" id="observedsubject-gene-summary-row-tmpl">
        <tr>
            <td id="subject-image-{{id}}"></td>
            <td id="subject.displayName-{{id}}">
                <a href="#/subject/{{subject.id}}">
                    {{subject.displayName}}
                </a>  &nbsp;
                <a href="#" class="addGene-{{subject.displayName}} greenColor" title="Add gene to cart" >+</a>			  				 
            </td>
            <td>{{subject.type}}</td>
            <td>{{observedSubjectRole.subjectRole.displayName}}</td>
            <td>{{observedSubjectRole.displayText}}</td>
        </tr>
    </script>

    <script type="text/template" id="search-results-gene-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/gene.png" class="img-polaroid search-info" title="Gene" alt="Gene" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="search-results-shrna-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/shrna.png" class="img-polaroid search-info" title="shRNA" alt="shRNA" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="search-results-animalmodel-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/animalmodel.png" title="Animal model" alt="Animal model" class="img-polaroid search-info" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="search-results-cellsample-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/cellsample.png" title="Cell sample" alt="Cell sample" class="img-polaroid search-info" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="search-results-tissuesample-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/tissuesample.png" title="Tissue sample" alt="Tissue sample" class="img-polaroid search-info" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="search-results-unknown-image-tmpl">
        <a href="#subject/{{id}}">
            <img src="img/unknown.png" title="{{type}}" class="img-polaroid search-info" alt="{{type}}" height="50" width="50">
        </a>
    </script>

    <script type="text/template" id="observedmrafileevidence-row-tmpl">
        <tr>
            <td>
                <img src="img/icons/{{observedEvidenceRole.evidenceRole.displayName}}.png" class="img-rounded" title="{{observedEvidenceRole.evidenceRole.displayName}}" alt="{{observedEvidenceRole.evidenceRole.displayName}}">
            </td>
            <td>{{observedEvidenceRole.evidenceRole.displayName}}</td>
            <td>{{observedEvidenceRole.displayText}}</td>
            <td>
                <div class="dropdown">
                    ( <a class="dropdown-toggle" data-toggle="dropdown" href="#">view mra file<b class="caret"></b></a> )
                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
                        <li>
                            <a href="#/evidence/{{id}}" title="Open Master Regulator View" class="desc-tooltip">
                                mra view
                            </a>
                        </li>
                        <li>
                            <a href="<%=dataURL%>{{evidence.filePath}}" class="desc-tooltip" target="_blank" title="type: ({{evidence.mimeType}})">view in browser</a>
                        </li>
                    </ul>
                </div>
            </td>
        </tr>
    </script>  

    <script type="text/template" id="existing-template-row-tmpl">
        <tr id="template-table-row-{{id}}" class='stored-template-row'>
            <td>{{displayName}}</td><td>{{description}}</td><td>{{project}}</td>
                <td>{{tier}}</td><td>{{dateLastModified}}</td><td>{{isStory}}</td>
                <td><select id='template-action-{{id}}'>
                    <option value=''>-</option>
                    <option value='edit'>Edit</option>
                    <option value='preview'>Preview</option>
                    <option value='clone'>Clone</option>
                    <option value='delete'>Delete</option>
                    <option value='download'>Download</option>
                    </select>
                </td>
        </tr>
    </script>

    <script type="text/template" id="role-dropdown-row-tmpl">
        <option {{selected}}>{{roleName}}</option>
    </script>

    <script type="text/template" id="template-subject-data-row-tmpl">
        <tr id="template-subject-row-columntag-{{columnTagId}}" class="template-data-row">
            <td style="text-align:center;"><img src="img/icons/remove.png" style="width:20px;height:20px;" id="delete-subject-{{columnTagId}}" /></td>
            <td><select id="subject-class-dropdown-{{columnTagId}}" class='subject-classes'>
                <option {{ subjectClass=='gene'?'selected=selected':'' }} >gene</option>
                <option {{ subjectClass=='shrna'?'selected=selected':'' }} >shrna</option>
                <option {{ subjectClass=='tissue_sample'?'selected=selected':'' }} >tissue_sample</option>
                <option {{ subjectClass=='cell_sample'?'selected=selected':'' }} >cell_sample</option>
                <option {{ subjectClass=='compound'?'selected=selected':'' }}>compound</option>
                <option {{ subjectClass=='animal_model'?'selected=selected':'' }} >animal_model</option>
            </select></td>
            <td><select id="role-dropdown-{{columnTagId}}" class='subject-roles'></select></td>
            <td><input type="text" class="form-control subject-columntag" value="{{columnTag}}" placeholder="column tag"></td>
            <td><input type="text" class="form-control subject-descriptions collapsed-textarea" id="description-{{columnTagId}}" placeholder="subject description" value="{{subjectDescription}}"></td>
        </tr>
    </script>

    <script type="text/template" id="evidence-type-dropdown-tmpl">
        <option {{ selected?'selected=selected':'' }} >{{evidenceType}}</option>
    </script>

    <script type="text/template" id="template-evidence-data-row-tmpl">
        <tr id="template-evidence-row-columntag-{{columnTagId}}" class="template-data-row">
            <td style="text-align:center;"><img src="img/icons/remove.png" style="width:20px;height:20px;" id="delete-evidence-{{columnTagId}}" /></td>
            <td><select id="value-type-{{columnTagId}}" class='value-types'>
                <option {{ valueType=='numeric'?'selected=selected':'' }} >numeric</option>
                <option {{ valueType=='label'?'selected=selected':'' }} >label</option>
                <option {{ valueType=='file'?'selected=selected':'' }} >file</option>
                <option {{ valueType=='url'?'selected=selected':'' }} >url</option>
            </select></td>
            <td><select id="evidence-type-{{columnTagId}}" class='evidence-types'></select></td>
            <td><input type="text" class="form-control evidence-columntag" value="{{columnTag}}" placeholder="column tag"></td>
            <td><input type="text" class="form-control evidence-descriptions collapsed-textarea" id="evd-descr-{{columnTagId}}" placeholder="evidence description" value="{{evidenceDescription}}"></td>
        </tr>
    </script>

    <script type="text/template" id="submitter-information-tmpl">
        <tr><th>Submitter First Name *</th>
            <td><input id="first-name" placeholder="first name is required" class="input-xxxlarge" value="{{firstName}}"></td></tr>
        <tr><th>Submitter Last Name *</th><td>
            <input id="last-name" placeholder="last name is required" class="input-xxxlarge" value="{{lastName}}"></td></tr>
        <tr><th>Contact E-mail</th><td><input id="email" placeholder="email is required" class="input-xxxlarge" value="{{email}}"></td></tr>
        <tr><th>Contact Phone Number</th>
            <td><input id="phone" placeholder="phone number is optional" class="input-xxxlarge" value="{{phone}}"></td></tr>
    </script>

    <script type="text/template" id="template-description-tmpl">
        <tr><th>Submission Name *</th>
            <td><input id="template-name" placeholder="e.g. centername_your_description" class="input-xxxlarge" value="{{displayName}}"></td></tr>
        <tr><th>Submission Description</th>
            <td><textarea id="template-submission-desc" placeholder="e.g. Down-regulated genes in PTEN-null cell lines" class="input-xxxlarge">{{description}}</textarea></td>
        </tr>
        <tr><th>Project Title</th>
            <td><input id="template-project-title" placeholder="Please enter a title for this or a collection of related subissions (correponds e.g. to an entire paper)" class="input-xxxlarge" value="{{project}}">
            </td>
        </tr>
        <tr><th>Request Tier</th>
            <td><select id="template-tier" class="input-xxxlarge">
                <option value=1 {{tier==1?'selected=selected':null}}>Tier 1 (initial or screening)</option>
                <option value=2 {{tier==2?'selected=selected':null}}>Tier 2 (in vitro)</option>
                <option value=3 {{tier==3?'selected=selected':null}}>Tier 3 (in vivo validation)</option>
            </select></td>
        </tr>
        <tr><th>Is this submission a story?</th><td><input id="template-is-story" type="checkbox" {{isStory?'checked':''}} /></td></tr>
        <tr id='story-title-row'><th>Story Title</th><td><input id='story-title' class="input-xxxlarge" value='{{storyTitle}}' /></td></tr>
        <tr><th>PI</th><td><input id="pi-name" class="input-xxxlarge" value='{{piName}}'/></td></tr>
    </script>

    <script type="text/template" id="template-helper-tmpl">
        <div class="container common-container" id="template-helper-container">
            <h2>CTD<sup>2</sup> Dashboard Submission Builder</h2>

            <div class="pull-right">
                <a href="https://ocg.cancer.gov/sites/default/files/CTD2-DB-Submission-Builder-Tutorial.pdf" target="_blank" class="btn btn-info">Manual</a>
                <a href="https://www.ncbi.nlm.nih.gov/pubmed/29220450" target="_blank" class="btn btn-info">Manuscript</a>
                <a href="https://ocg.cancer.gov/sites/default/files/Glossary-CTD2-Dashboard.pdf" target="_blank" class="btn btn-info">Glossary</a>
            </div>

            <div class="btn-group" role="group" aria-label="...">
            <button type="button" class="btn btn-link" id='menu_home'>Home</button>
            <button type="button" class="btn btn-link" id='menu_manage'>Manage Submission</button>
            </div>
            <div class="btn-group" role="group" aria-label="...">
            <button type="button" class="btn btn-link" id='menu_description'>Submission Description</button>
            <button type="button" class="btn btn-link" id='menu_data'>Submission Data</button>
            <button type="button" class="btn btn-link" id='menu_summary'>Observation Summary</button>
            <button type="button" class="btn btn-link" id='menu_preview'>Preview</button>
            </div>

            <div id="step1">
                <div class="alert alert-warning alert-block">
                  <a href="#" class="close" data-dismiss="alert">&times;</a>
                  <p>
                    <strong>Welcome to the Submission Builder</strong><br>
                    This tool will help create a basic Dashboard submission template from scratch.
                    Once a basic template is prepared, the template can be downloaded for local use and preparation of a Dashboard submission.
                  </p>
                </div>
                <h3>Submission Builder Home</h3>
                <table class="table">
                    <tr>
                        <th>Please choose your CTD<sup>2</sup> center: </th>
                        <td>
                            <select id="template-submission-centers" class="input-xxlarge">
                                <option value="">-</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan=2 class="next-cell">
                            <button id="apply-submission-center" class="btn btn-outline-dark">Continue</button>
                        </td>
                    </tr>
                </table>
            </div>

            <div id="step2" class="hide">
                <h3>Manage Submission</h3>
                <b>Center:</b> <span id="center-name"></span>
                <div class="alert alert-warning alert-block">
                <a href="#" class="close" data-dismiss="alert">&times;</a>
                <p>
                    A submission is a collection of one or more observations that summarize the results of experimental or computational investigations. A scientific publication can give rise to one or multiple such submissions. For example, a single publication may advance a hypothesis step by step, with each step potentially meriting a separate submission. 
                </p>
                </div>
                <div class="alert alert-warning alert-block">
                <a href="#" class="close" data-dismiss="alert">&times;</a>
                <p>
                    You will be able to reuse information entered for one submission in additional, related submissions. A project title is used to group together such related submissions. 
                </p>
                </div>
                <table class="table table-bordered table-striped" id="existing-template-table">
                        <tr id="template-header">
                            <th>Submission Name</th><th>Submission Description</th><th>Project</th><th>Tier</th><th>Date last modified</th><th>Is Story</th><th>Action</th>
                        </tr>
                </table>

                <table  class="table">
                    <tr>
                        <td class="next-cell">
                            <button id="create-new-submission" class="btn btn-outline-dark">Create New Submission</button>
                        </td>
                    </tr>
                </table>
            </div>

            <div id="step3" class="hide">
                <h3>Submitter Information</h3>
                <b>Center:</b> <span id="center-name"></span>
                <table class="table" id="submitter-information"></table>
                <h3>Submission Description</h3>
                <table class="table" id="template-description"></table>
                <table class="table">
                    <tr>
                        <td class="next-cell"><button id="save-name-description">Save</button></td>
                        <td class="next-cell"><button id="continue-to-main-data">Save and Continue</button></td>
                    </tr>
                </table>
            </div>

            <div id="step4" class="hide">
                <h3>Submission Data</h3>
                <b>Center:</b> <span id="center-name"></span><br/>
                <b>Submission Name:</b> <span id="submission-name"></span>
                <!-- this table contains the main data, which dictate the data structure I should use -->
                <div style="overflow-x:auto;">
                <table class="table table-bordered table-striped" id="template-table">
                    <tbody id="template-table-subject">
                        <tr><th colspan=5 style="background-color:#e6f6ff;">Subject</tr>
                        <tr id="subject-header">
                            <th>Delete Row</th><th>Subject Class</th><th>Subject Role</th><th>Column Tag</th><th>Description</th>
                        </tr>
                        <!-- here goes rows for subject columns -->
                    </tbody>
                    <tbody id="template-table-evidence">
                        <tr><th colspan=5 style="background-color:#e6f6ff;">Evidence</tr>
                        <tr id="evidence-header">
                            <th>Delete Row</th><th>Value Type</th><th>Evidence Type</th><th>Column Tag</th><th>Description</th>
                        </tr>
                        <!-- here goes the rows for evidence columns -->
                    </tbody>
                </table>
                </div>
                <table class="table">
                    <tr>
                        <td><button id="add-subject" class="next-cell">New Subject Row</button></td>
                        <td><button id="add-evidence" class="next-cell">New Evidence Row</button></td>
                        <td><button id="add-observation" class="next-cell">New Observation Column</button></td>
                    </tr>
                    <tr>
                        <td><button id="save-template-submission-data" class="next-cell">Save</button></td>
                        <td><button id="apply-template-submission-data" class="next-cell proceed-to-next-page">Save and Continue</button></td>
                    </tr>
                </table>
            </div>

            <div id="step5" class="hide">
                <h3>Observation Summary</h3>
                <b>Center:</b> <span id="center-name"></span><br/>
                <b>Submission Name:</b> <span id="submission-name"></span><br/>
                <ul>
                    <li>Please write/update a summary of the observations, using any or all of the "tags" pre-entred in the text box.</li>
                    <li>On the dashboard, the tag will be replaced with the actual values you submit.</li>
                    <li>Delete tags you do not need.</li>
                    <li>Use the Helper to see all available tags.</li>
                </ul>
                <table class="table">
                    <tr>
                        <th>Observation summary</th>
                        <td>
                            <textarea id="template-obs-summary" placeholder="e.g. <gene_column> is down-regulated in <label_evidence> cells" class="input-xxxlarge"></textarea>
                        </td>
                        <td>
                        <span class="dropdown">
                            <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">Helper
                            <span class="caret"></span></button>
                            <ul class="dropdown-menu" id='column-tag-list'></ul>
                        </span> 
                        </td>
                    </tr>
                </table>
                <button id="save-summary">Save</button>
                <button id="continue-from-summary">Save and Continue</button>

                <form action="template/download" method="POST" id="download-form" style="display:inline">
                            <button id="download-template">Download template</button>
                            <input type="hidden" name="template-id" id="template-id">
                            <input type="hidden" name="filename" id="filename-input">
                </form>
            </div> <!-- end of step 5 -->

            <div id="step6" class="hide">
                <h3>Preview</h3>
                <b>Center:</b> <span id="center-name"></span><br/>
                <b>Submission Name:</b> <span id="submission-name"></span><br/>
                <select id="preview-select"></select>
                <div id='preview-container'></div>
                <button id="download-from-preview" style="display: block; margin: auto">Download template</button>
            </div> <!-- end of step 6 -->

        </div><!-- end of template-helper-container -->
    </script>

    <script type="text/template" id="observation-option-tmpl">
        <option value={{observation_id}}>observation {{observation_id+1}}</option>
    </script>

    <script type="text/template" id="column-tag-item-tmpl">
        <li id={{id}} class=helper-tag>{{tag}}</li>
    </script>

    <script type="text/template" id="temp-observation-tmpl">
        <td><div class='uploaded'>{{uploaded}}</div><input type="{{type}}" class="form-control" id="observation-{{obvNumber}}-{{obvColumn}}" value="{{obvText}}" placeholder="enter value"></td>
    </script>

    <script type="text/template" id="template-helper-center-tmpl">
        <option value="{{id}}">{{displayName}}</option>
    </script>

    <script type="text/template" id="help-navigate-tmpl">
        <div class="help-navigate-text-container">
            <h3>Navigating and Understanding Dashboard Content</h3>
            <p>
                The CTD<sup>2</sup> Network aims to increase understanding of the underlying molecular causes of distinct cancer types and accelerate development of clinically useful biomarkers and targeted therapies for precision medicine.
                The Dashboard is one tool that provides access to Network findings.
                Results are available as bulk datasets, data-related figures, or polished stories, and are formatted to enable navigation and comprehension by most researchers, from computational experts to those with little bioinformatics dexterity.
                Through the Dashboard, the <b>CTD<sup>2</sup> Network</b> gives the research community a method to interrogate experimental observations across the Centers.
                Before using the Dashboard, read the following to learn how Dashboard content is organized.
            </p>

            <ul>
                <li><i>Center</i>: One of 13 academic research teams that make up the CTD<sup>2</sup> Network. To learn more about the current Centers, visit <a target="_blank" href="https://ocg.cancer.gov/programs/ctd2/centers">https://ocg.cancer.gov/programs/ctd2/centers</a>.</li>

                <li><i>Submission</i>: A Dashboard entry that represents a dataset associated with positive experimental results, a set of data-related figures, or a polished story.</li>

                <li><i>Subject</i>: The focus of an experiment or result in a Dashboard <b>submission</b> (<i>e.g.</i>, genes, proteins, small molecules, cell lines, animal models).</li>
                <ul>
                    <li>Class</i>: A set of objects representing the same molecular or biological category (DNA, RNA, protein, small molecule, tissue, animal model) and sharing a set of required and optional attributes.</li>
                    <li><i>Role</i>: The <b>Center</b>-designated function of a gene, protein, or compound based on their interpretation of observations within a particular experimental or computational context. Assigning <b>role</b>s from a restricted list of terms (biomarkers, diseases, master regulators, interactors, oncogenes, perturbagens, candidate drugs, or targets) helps organize subjects in Dashboard for browsing and searching.
                </ul>

                <li><i>Evidence</i>: Selected positive or validated results from a scientific experiment (<i>e.g.</i>, numeric values, text labels, data figures).</li>
                <ul>
                    <li><i>Type</i>:  Category of evidence provided in support of the results. Examples include the following: literature, measured, link, reference, background, observed, computed, written, resources, species.
                </ul>

                <li><i>Observation</i>: A <b>Center</b>-determined conclusion that is submitted as a connection between <b>subjects</b> and <b>evidence</b>; the "fundamental unit" of the Dashboard.</li>

                <li><i><a href="http://www.ncbi.nlm.nih.gov/pubmed/27401613" target="_blank">Tier</a></i>: A CTD<sup>2</sup> Network-defined ranking system for <b>evidence</b> that is based on the extent of characterization associated with a particular study.
                    <ul>
                        <li><i>Tier 1</i>: Preliminary positive observations.</li>
                        <li><i>Tier 2</i>: Confirmation of primary results <i>in vitro</i>.</li>
                        <li><i>Tier 3</i>: Validation of results in a cancer relevant <i>in vivo</i> model.</li>
                    </ul>
                </li>
            </ul>

            <h3>Dashboard Organization</h3>
            <p>
                During the <b>submission</b> process, <b>subjects</b> from CTD2 Network studies are ordered by relevance based on the number of <b>observations</b> associated with a <b>submission</b>, the <b>Tiers</b> of these <b>observations</b>, and the number of different <b>Centers</b> providing <b>observations</b> about that particular <b>subject</b>.
            </p>

            <br>
            <hr>
            <br>
        </div>
    </script>

    <!-- end of templates -->

    <script src="js/underscore-min.js"></script>
    <script src="js/backbone-min.js"></script>
    <script src="js/bootstrap.bundle.min.js"></script>
    <script src="js/jquery.fancybox.min.js"></script>
    <script src="js/ctd2.js"></script>
    <script src="js/template.helper.js?ts=2019"></script>

  </body>
</html>
