package com.proptiger.userservice.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import com.proptiger.core.model.filter.AbstractQueryBuilder;
import com.proptiger.core.model.filter.JPAQueryBuilder;
import com.proptiger.core.model.proptiger.Dashboard;
import com.proptiger.core.pojo.FIQLSelector;

public class DashboardDaoImpl {
    @Autowired
    public EntityManagerFactory emf;

    public List<Dashboard> getDashboards(FIQLSelector fiqlSelector) {
        EntityManager em = emf.createEntityManager();
        AbstractQueryBuilder<Dashboard> builder = new JPAQueryBuilder<>(em, Dashboard.class);
        builder.buildQuery(fiqlSelector);
        List<Dashboard> list = builder.retrieveResults();
        em.close();
        return list;
    }
}
