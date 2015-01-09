package com.proptiger.userservice.repo;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proptiger.core.model.user.User;

/**
 * 
 * @author azi
 * 
 */

public interface UserDao extends JpaRepository<User, Integer>, UserCustomDao {
    public User findByEmail(String email);
    
    @Query("select U from User U left join fetch U.userRoles UR left join fetch UR.role MR where U.email = ?1")
    public User findByEmailWithRoles(String email);
    
    @Query("select U from User U left join fetch U.userRoles UR left join fetch UR.role MR where U.id = ?1")
    public User findByIdWithRoles(int userId);
    
    @Query("SELECT U FROM User U join U.userAuthProviderDetails APD WHERE " + " APD.providerId = ?1 AND APD.providerUserId = ?2")
    public User findByProviderIdAndProviderUserId(int providerId, String providerUserId);

    @Query("select U from User U join U.contactNumbers CN where (U.email = ?1 or CN.contactNumber = ?2)")
    public User findByPrimaryEmailOrPhone(String email,String contactNumber);
    
    @Query("select U from User U join U.contactNumbers CN where (CN.contactNumber = ?1 and U.id = ?2)")
    public User findByPhone(String contactNumber, int userId);

    @Query("select U from User U left join fetch U.contactNumbers CN left join fetch U.userAuthProviderDetails where U.id = ?1")
    public User findByUserIdWithContactAndAuthProviderDetails(int id);

    public List<User> findByIdIn(Set<Integer> userIds);
    
}
