package com.zhuaer.learning.dynamic.datasource.springdatajpa.repository.users;

import com.zhuaer.learning.dynamic.datasource.springdatajpa.dataobject.UserDO;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserDO, Integer> {

}
