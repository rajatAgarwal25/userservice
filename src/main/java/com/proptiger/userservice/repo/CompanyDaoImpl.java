package com.proptiger.userservice.repo;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import com.proptiger.core.model.cms.Company;
import com.proptiger.core.model.filter.AbstractQueryBuilder;
import com.proptiger.core.model.filter.JPAQueryBuilder;
import com.proptiger.core.pojo.FIQLSelector;

/**
 * @author Rajeev Pandey
 *
 */
public class CompanyDaoImpl {
    @Autowired
    public EntityManagerFactory emf;

    public List<Company> getCompanies(FIQLSelector fiqlSelector) {
        EntityManager em = emf.createEntityManager();
        AbstractQueryBuilder<Company> builder = new JPAQueryBuilder<>(em, Company.class);
        builder.buildQuery(fiqlSelector);
        List<Company> list = builder.retrieveResults();
        em.close();
        return list;

    }
}
