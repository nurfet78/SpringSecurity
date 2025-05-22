package org.nurfet.springsecurity.service;

import org.nurfet.springsecurity.model.Role;

import java.util.List;

public interface RoleService {

    List<Role> findAllRoles();
}
