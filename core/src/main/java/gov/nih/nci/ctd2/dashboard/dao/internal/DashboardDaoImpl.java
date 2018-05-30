package gov.nih.nci.ctd2.dashboard.dao.internal;

import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.model.*;
import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.*;

public class DashboardDaoImpl extends HibernateDaoSupport implements DashboardDao {

    private DashboardFactory dashboardFactory;

    public DashboardFactory getDashboardFactory() {
        return dashboardFactory;
    }

    public void setDashboardFactory(DashboardFactory dashboardFactory) {
        this.dashboardFactory = dashboardFactory;
    }

    @Override
    public void save(DashboardEntity entity) {
        getHibernateTemplate().save(entity);
    }

    @Override
    public void batchSave(Collection<? extends DashboardEntity> entities, int batchSize) {
        if(entities == null || entities.isEmpty())
            return;

        ArrayList<DashboardEntity> allEntities = new ArrayList<DashboardEntity>();
        for (DashboardEntity entity : entities) {
            if(entity instanceof Subject) {
                Subject subject = (Subject) entity;
                allEntities.addAll(subject.getXrefs());
                allEntities.addAll(subject.getSynonyms());
            }
        }
        allEntities.addAll(entities);

        // Insert new element super fast with a stateless session
        StatelessSession statelessSession = getHibernateTemplate().getSessionFactory().openStatelessSession();
        Transaction tx = statelessSession.beginTransaction();

        for (DashboardEntity entity : allEntities)
            statelessSession.insert(entity);

        tx.commit();
        statelessSession.close();

        // And then update them all to create the actual mappings
        Session session = getHibernateTemplate().getSessionFactory().openSession();
        int i = 0;
        for (DashboardEntity entity : allEntities) {
            session.update(entity);
            if(++i % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        session.flush();
        session.clear();
        session.close();
    }

    @Override
    public void update(DashboardEntity entity) {
        getHibernateTemplate().update(entity);
    }

    @Override
    public void merge(DashboardEntity entity) {
        getHibernateTemplate().merge(entity);
    }


    @Override
    public void delete(DashboardEntity entity) {
        getHibernateTemplate().delete(entity);
    }

    @Override
    public <T extends DashboardEntity> T getEntityById(Class<T> entityClass, Integer id) {
        Class<T> aClass = entityClass.isInterface()
                ? dashboardFactory.getImplClass(entityClass)
                : entityClass;
        return getHibernateTemplate().get(aClass, id);
    }

    @Override
    public Long countEntities(Class<? extends DashboardEntity> entityClass) {
        Criteria criteria = getSession().createCriteria(dashboardFactory.getImplClass(entityClass));
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public <T extends DashboardEntity> List<T> findEntities(Class<T> entityClass) {
        List<T> list = new ArrayList<T>();
        Class<T> implClass = dashboardFactory.getImplClass(entityClass);
        Criteria criteria = getSession().createCriteria(implClass);
        for (Object o : criteria.list()) {
            assert implClass.isInstance(o);
            list.add((T) o);
        }
        return list;
    }

    @Override
    @Cacheable(value = "browseCompoundCache")
    public List<Compound> browseCompounds(String startsWith) {
        List<Compound> list = new ArrayList<Compound>();
        for (Object o : getHibernateTemplate().find("from CompoundImpl where displayName LIKE CONCAT(?, '%')", startsWith)) {
            assert o instanceof Compound;
            Compound compound = (Compound) o;
            if(!findObservedSubjectBySubject(compound).isEmpty())
                list.add(compound);
        }
        return list;
    }

    @Override
    public List<ObservationTemplate> findObservationTemplateBySubmissionCenter(SubmissionCenter submissionCenter) {
        List<ObservationTemplate> list = new ArrayList<ObservationTemplate>();
        for (Object o : getHibernateTemplate().find("from ObservationTemplateImpl where submissionCenter = ?", submissionCenter)) {
            assert o instanceof ObservationTemplate;
            list.add((ObservationTemplate) o);
        }

        return list;
    }

    @Override
    @Cacheable(value = "browseTargetCache")
    public List<Gene> browseTargets(String startsWith) {
        List<Gene> list = new ArrayList<Gene>();
        for (Object o : getHibernateTemplate().find("from GeneImpl where displayName LIKE CONCAT(?, '%')", startsWith)) {
            assert o instanceof Gene;
            Gene gene = (Gene) o;
            if(!findObservedSubjectBySubject(gene).isEmpty())
                list.add(gene);
        }
        return list;
    }

    @Override
    public List<Gene> findGenesByEntrezId(String entrezId) {
        List<Gene> list = new ArrayList<Gene>();
        for (Object o : getHibernateTemplate().find("from GeneImpl where entrezGeneId = ?", entrezId)) {
            assert o instanceof Gene;
            list.add((Gene) o);
        }
        return list;
    }

    @Override
    public List<Gene> findGenesBySymbol(String symbol) {
        List<Gene> list = new ArrayList<Gene>();
        for (Object o : getHibernateTemplate().find("from GeneImpl where displayName = ?", symbol)) {
            assert o instanceof Gene;
            list.add((Gene) o);
        }
        return list;
    }

    @Override
    public List<Protein> findProteinsByUniprotId(String uniprotId) {
        List<Protein> list = new ArrayList<Protein>();
        for (Object o : getHibernateTemplate().find("from ProteinImpl where uniprotId = ?", uniprotId)) {
            assert o instanceof Protein;
            list.add((Protein) o);
        }
        return list;
    }

    @Override
    public List<Transcript> findTranscriptsByRefseqId(String refseqId) {
        String[] parts = refseqId.split("\\.");
        List<Transcript> list = new ArrayList<Transcript>();
        for (Object o : getHibernateTemplate().find("from TranscriptImpl where refseqId like ?", parts[0] + "%")) {
            assert o instanceof Transcript;
            list.add((Transcript) o);
        }
        return list;
    }

	@Override
    public List<CellSample> findCellSampleByAnnoType(String type) {
		List<CellSample> cellSamples = new ArrayList<CellSample>();

        // first grab annotations by type
        for (Object anno : getHibernateTemplate().find("from AnnotationImpl where type = ?", type)) {
            assert anno instanceof Annotation;
            for (Object cellSample : getHibernateTemplate().find("from CellSampleImpl as cs where ? member of cs.annotations", (Annotation)anno)) {
                assert cellSample instanceof CellSample;
                if (!cellSamples.contains(cellSample)) {
                    cellSamples.add((CellSample)cellSample);
                }
            }
        }

        return cellSamples;
	}

	@Override
    public List<CellSample> findCellSampleByAnnoSource(String source) {
		List<CellSample> cellSamples = new ArrayList<CellSample>();

        // first grab annotations by source
        for (Object anno : getHibernateTemplate().find("from AnnotationImpl where source = ?", source)) {
            assert anno instanceof Annotation;
            for (Object cellSample : getHibernateTemplate().find("from CellSampleImpl as cs where ? member of cs.annotations", (Annotation)anno)) {
                assert cellSample instanceof CellSample;
                if (!cellSamples.contains(cellSample)) {
                    cellSamples.add((CellSample)cellSample);
                }
            }
        }

        return cellSamples;
	}

	@Override
    public List<CellSample> findCellSampleByAnnoName(String name) {
		List<CellSample> cellSamples = new ArrayList<CellSample>();

        // first grab annotations by source
        for (Object anno : getHibernateTemplate().find("from AnnotationImpl where displayName = ?", name)) {
            assert anno instanceof Annotation;
            for (Object cellSample : getHibernateTemplate().find("from CellSampleImpl as cs where ? member of cs.annotations", (Annotation)anno)) {
                assert cellSample instanceof CellSample;
                if (!cellSamples.contains(cellSample)) {
                    cellSamples.add((CellSample)cellSample);
                }
            }
        }

        return cellSamples;
	}

    @Override
    public List<CellSample> findCellSampleByAnnotation(Annotation annotation) {
        List<CellSample> list = new ArrayList<CellSample>();
        for (Object cs : getHibernateTemplate().find("select cs from CellSampleImpl as cs where ? member of cs.annotations", annotation)) {
            assert cs instanceof CellSample;
            list.add((CellSample)cs);
        }
        return list;
    }

	@Override
    public List<TissueSample> findTissueSampleByName(String name) {
		List<TissueSample> samples = new ArrayList<TissueSample>();

        for (Object o : getHibernateTemplate().find("from TissueSampleImpl where displayName = ?", name)) {
            assert o instanceof TissueSample;
            samples.add((TissueSample) o);
        }
        return samples;
	}

    @Override
    public List<CellSample> findCellLineByName(String name) {
        List<CellSample> cellSamples = new ArrayList<CellSample>();
        for (Subject subject : findSubjectsBySynonym(name, true)) {
            if(subject instanceof CellSample) {
                cellSamples.add((CellSample) subject);
            }
        }
        return cellSamples;
    }

    @Override
    public List<ShRna> findSiRNAByReagentName(String reagent) {
        List<ShRna> list = new ArrayList<ShRna>();
        for (Object o : getHibernateTemplate().find("from ShRnaImpl where reagentName = ?", reagent)) {
            assert o instanceof ShRna;
            list.add((ShRna) o);
        }
        return list;
    }
    
    @Override
    public List<ShRna> findSiRNAByTargetSequence(String targetSequence) {
        List<ShRna> list = new ArrayList<ShRna>();
        for (Object o : getHibernateTemplate().find("from ShRnaImpl where targetSequence = ?", targetSequence)) {
            assert o instanceof ShRna;
            list.add((ShRna) o);
        }
        return list;
    }

	@Override
    public List<Compound> findCompoundsByName(String compoundName) {
		List<Compound> compounds = new ArrayList<Compound>();

        for (Object o : getHibernateTemplate().find("from CompoundImpl where displayName = ?", compoundName)) {
            assert o instanceof Compound;
            compounds.add((Compound) o);
        }

        return compounds;
	}

    @Override
    public List<Compound> findCompoundsBySmilesNotation(String smilesNotation) {
        List<Compound> list = new ArrayList<Compound>();
        for (Object o : getHibernateTemplate().find("from CompoundImpl where smilesNotation = ?", smilesNotation)) {
            assert o instanceof Compound;
            list.add((Compound) o);
        }
        return list;
    }

	@Override
    public List<AnimalModel> findAnimalModelByName(String animalModelName) {
		List<AnimalModel> models = new ArrayList<AnimalModel>();

        for (Object o : getHibernateTemplate().find("from AnimalModelImpl where displayName = ?", animalModelName)) {
            assert o instanceof AnimalModel;
            models.add((AnimalModel) o);
        }
        return models;
	}

    @Override
    public List<Subject> findSubjectsByXref(String databaseName, String databaseId) {
        Set<Subject> subjects = new HashSet<Subject>();
        List list = getHibernateTemplate()
                        .find("from XrefImpl where databaseName = ? and databaseId = ?", databaseName, databaseId);
        for (Object o : list) {
            assert o instanceof Xref;
            subjects.addAll(findSubjectsByXref((Xref) o));
        }

        return new ArrayList<Subject>(subjects);
    }

    @Override
    public List<Subject> findSubjectsByXref(Xref xref) {
        List<Subject> list = new ArrayList<Subject>();
        for (Object o : getHibernateTemplate().find("select o from SubjectImpl as o where ? member of o.xrefs", xref)) {
            assert o instanceof Subject;
            list.add((Subject) o);
        }
        return list;
    }

    @Override
    public List<Organism> findOrganismByTaxonomyId(String taxonomyId) {
        List<Organism> list = new ArrayList<Organism>();
        for (Object o : getHibernateTemplate().find("from OrganismImpl where taxonomyId = ?", taxonomyId)) {
            assert o instanceof Organism;
            list.add((Organism) o);
        }
        return list;
    }

    @Override
    public List<SubjectWithOrganism> findSubjectByOrganism(Organism organism) {
        List<SubjectWithOrganism> list = new ArrayList<SubjectWithOrganism>();
        for (Object o : getHibernateTemplate().find("from SubjectWithOrganismImpl where organism = ?", organism)) {
            assert o instanceof SubjectWithOrganism;
            list.add((SubjectWithOrganism) o);
        }

        return list;
    }

    @Override
    public List<Subject> findSubjectsBySynonym(String synonym, boolean exact) {
        Set<Subject> subjects = new HashSet<Subject>();

        // First grab the synonyms
        String query = "from SynonymImpl where displayName "
                + (exact ? " = ?" : "like concat('%', ?, '%')");
        for (Object o : getHibernateTemplate().find(query, synonym)) {
            assert o instanceof Synonym;

            // Second: find subjects with the synonym
            List subjectList = getHibernateTemplate()
                    .find("select o from SubjectImpl as o where ? member of o.synonyms", (Synonym) o);
            for (Object o2 : subjectList) {
                assert o2 instanceof Subject;
                subjects.add((Subject) o2);
            }
        }

        return new ArrayList<Subject>(subjects);
    }

    @Override
    public ObservedSubjectRole findObservedSubjectRole(String templateName, String columnName) {
        List<ObservedSubjectRole> list = new ArrayList<ObservedSubjectRole>();
		// first grab observation template name
		for (Object ot : getHibernateTemplate()
				 .find("from ObservationTemplateImpl where displayName = ?", templateName)) {
			assert ot instanceof ObservationTemplate;
			for (Object o : getHibernateTemplate().
					 find("from ObservedSubjectRoleImpl as osr where columnName = ? and " +
						  "osr.observationTemplate = ?", columnName, (ObservationTemplate)ot)) {
				assert o instanceof ObservedSubjectRole;
				list.add((ObservedSubjectRole) o);
			}
		}
		assert list.size() <= 1;
		return (list.size() == 1) ? list.iterator().next() : null;
    }

    @Override
    public ObservedEvidenceRole findObservedEvidenceRole(String templateName, String columnName) {
        List<ObservedEvidenceRole> list = new ArrayList<ObservedEvidenceRole>();
		// first grab observation template name
		for (Object ot : getHibernateTemplate()
				 .find("from ObservationTemplateImpl where displayName = ?", templateName)) {
			assert ot instanceof ObservationTemplate;
			for (Object o : getHibernateTemplate()
					 .find("from ObservedEvidenceRoleImpl as oer where columnName = ? and " +
						   "oer.observationTemplate = ?", columnName, (ObservationTemplate)ot)) {
				assert o instanceof ObservedEvidenceRole;
				list.add((ObservedEvidenceRole) o);
			}
		}
		assert list.size() <= 1;
		return (list.size() == 1) ? list.iterator().next() : null;
    }

	@Override
    public ObservationTemplate findObservationTemplateByName(String templateName) {
		List<ObservationTemplate> list = new ArrayList<ObservationTemplate>();
        for (Object o : getHibernateTemplate()
				 .find("from ObservationTemplateImpl where displayName = ?", templateName)) {
            assert o instanceof ObservationTemplate;
            list.add((ObservationTemplate) o);
        }
		assert list.size() <= 1;
		return (list.size() == 1) ? list.iterator().next() : null;
	}

	@Override
    public SubmissionCenter findSubmissionCenterByName(String submissionCenterName) {
		List<SubmissionCenter> list = new ArrayList<SubmissionCenter>();
        for (Object o : getHibernateTemplate()
				 .find("from SubmissionCenterImpl where displayName = ?", submissionCenterName)) {
            assert o instanceof SubmissionCenter;
            list.add((SubmissionCenter) o);
        }
		assert list.size() <= 1;
		return (list.size() == 1) ? list.iterator().next() : null;
	}

    @Override
    public List<Submission> findSubmissionByIsStory(boolean isSubmissionStory, boolean sortByPriority) {
        List<Submission> list = new ArrayList<Submission>();
        List tmpList = sortByPriority
                ? getHibernateTemplate()
                    .find("from ObservationTemplateImpl where isSubmissionStory = ? order by submissionStoryRank desc", isSubmissionStory)
                : getHibernateTemplate().find("from ObservationTemplateImpl where isSubmissionStory = ?", isSubmissionStory);

        for (Object o : tmpList) {
            assert o instanceof ObservationTemplate;
            list.addAll(findSubmissionByObservationTemplate((ObservationTemplate) o));
        }

        return list;
    }

    @Override
    public List<Submission> findSubmissionByObservationTemplate(ObservationTemplate observationTemplate) {
        List<Submission> list = new ArrayList<Submission>();
        for (Object o : getHibernateTemplate()
                .find("from SubmissionImpl where observationTemplate = ?", observationTemplate)) {
            assert o instanceof Submission;
            list.add((Submission) o);
        }

        return list;
    }

	@Override
    public Submission findSubmissionByName(String submissionName) {
        List<Submission> submissions = new ArrayList<Submission>();

        for (Object o : getHibernateTemplate().find("from SubmissionImpl where displayName = ?", submissionName)) {
            assert o instanceof Submission;
            submissions.add((Submission)o);
        }

        assert submissions.size() <= 1;
        return (submissions.size() == 1) ? submissions.iterator().next() : null;
	}

    @Override
    public List<Submission> findSubmissionBySubmissionCenter(SubmissionCenter submissionCenter) {
        List<Submission> list = new ArrayList<Submission>();
        for (ObservationTemplate o : findObservationTemplateBySubmissionCenter(submissionCenter)) {
            list.addAll(findSubmissionByObservationTemplate(o));
        }

        return list;
    }

    @Override
    public List<Observation> findObservationsBySubmission(Submission submission) {
        List<Observation> list = new ArrayList<Observation>();
        for (Object o : getHibernateTemplate().find("from ObservationImpl where submission = ?", submission)) {
            assert o instanceof Observation;
            list.add((Observation) o);
        }

        return list;
    }

    @Override
    public List<ObservedSubject> findObservedSubjectBySubject(Subject subject) {
        List<ObservedSubject> list = new ArrayList<ObservedSubject>();
        for(Object o : getHibernateTemplate().find("from ObservedSubjectImpl where subject = ?", subject)) {
            assert o instanceof ObservedSubject;
            list.add((ObservedSubject) o);
        }

        return list;
    }

    @Override
    public List<ObservedSubject> findObservedSubjectByObservation(Observation observation) {
        List<ObservedSubject> list = new ArrayList<ObservedSubject>();
        for(Object o : getHibernateTemplate().find("from ObservedSubjectImpl where observation = ?", observation)) {
            assert o instanceof ObservedSubject;
            list.add((ObservedSubject) o);
        }

        return list;
    }

    @Override
    public List<ObservedEvidence> findObservedEvidenceByObservation(Observation observation) {
        List<ObservedEvidence> list = new ArrayList<ObservedEvidence>();
        for(Object o : getHibernateTemplate().find("from ObservedEvidenceImpl where observation = ?", observation)) {
            assert o instanceof ObservedEvidence;
            list.add((ObservedEvidence) o);
        }

        return list;
    }

    @Override
    public List<ObservedSubject> findObservedSubjectByRole(String role) {
        List<ObservedSubject> list = new ArrayList<ObservedSubject>();
        for (Object o : getHibernateTemplate().find("from ObservedSubjectImpl where observedSubjectRole.subjectRole.displayName = ?", role)) {
            assert o instanceof ObservedSubject;
            list.add((ObservedSubject) o);
        }
        return list;
    }

    @Cacheable(value = "uniprotCache")
    @Override
    public List<Protein> findProteinByGene(Gene gene) {
        Set<Protein> proteins = new HashSet<Protein>();
        for(Object t: getHibernateTemplate().find("from TranscriptImpl where gene = ?", gene)) {
            assert t instanceof Transcript;

            for(Object p: getHibernateTemplate().find("from ProteinImpl as p where ? member of p.transcripts", (Transcript) t)) {
                assert p instanceof Protein;
                proteins.add((Protein) p);
            }
        }

        return (new ArrayList<Protein>(proteins));
    }
}
