package com.skillbox.sw.repository;

import com.skillbox.sw.domain.Friendship;
import com.skillbox.sw.domain.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer>, JpaSpecificationExecutor<Friendship> {

  @Query("select p "
      + "from Friendship f1 "
      + "join Friendship f2 on f1.dstPerson = f2.srcPerson "
      + "join Person p on f2.dstPerson = p "
      + "where f1.srcPerson = :me and f2.friendshipStatus.code = 'FRIEND' and f2.dstPerson != :me"
  )
  Page<Person> getRecommendations(@Param("me") Person me, Pageable pageable);
}
