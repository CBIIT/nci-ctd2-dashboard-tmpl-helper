package gov.nih.nci.ctd2.dashboard.dao.internal;

import gov.nih.nci.ctd2.dashboard.dao.DashboardDao;
import gov.nih.nci.ctd2.dashboard.model.*;
import org.hibernate.*;
import org.hibernate.criterion.Projections;

import java.util.*;

public class DashboardDaoImpl implements DashboardDao {

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getSession() {
        Session session = getSessionFactory().openSession();
        return session;
    }

    private DashboardFactory dashboardFactory;

    public DashboardFactory getDashboardFactory() {
        return dashboardFactory;
    }

    public void setDashboardFactory(DashboardFactory dashboardFactory) {
        this.dashboardFactory = dashboardFactory;
    }

    @Override
    public void save(DashboardEntity entity) {
        Session session = getSession();
        session.save(entity);
        session.flush();
        session.close();
    }

    @Override
    public void update(DashboardEntity entity) {
        Session session = getSession();
        session.update(entity);
        session.flush();
        session.close();
    }

    @Override
    public void merge(DashboardEntity entity) {
        Session session = getSession();
        session.merge(entity);
        session.flush();
        session.close();
    }

    @Override
    public void delete(DashboardEntity entity) {
        Session session = getSession();
        session.delete(entity);
        session.flush();
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DashboardEntity> T getEntityById(Class<T> entityClass, Integer id) {
        Class<T> aClass = entityClass.isInterface() ? dashboardFactory.getImplClass(entityClass) : entityClass;
        Session session = getSession();
        Object object = session.get(aClass, id);
        session.close();
        return (T) object;
    }

    @Override
    public Long countEntities(Class<? extends DashboardEntity> entityClass) {
        Criteria criteria = getSession().createCriteria(dashboardFactory.getImplClass(entityClass));
        return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @SuppressWarnings("unchecked")
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
    public SubmissionCenter findSubmissionCenterByName(String submissionCenterName) {
        Session session = getSession();
        org.hibernate.Query query = session.createQuery("from SubmissionCenterImpl where displayName = :cname");
        query.setParameter("cname", submissionCenterName);
        @SuppressWarnings("unchecked")
        List<SubmissionCenter> list = query.list();
        session.close();

        assert list.size() <= 1;
        return (list.size() == 1) ? list.iterator().next() : null;
    }

}
