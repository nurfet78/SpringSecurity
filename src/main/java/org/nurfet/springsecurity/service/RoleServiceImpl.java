package org.nurfet.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.nurfet.springsecurity.model.Role;
import org.nurfet.springsecurity.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;


    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
