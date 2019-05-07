package com.kotomka.core.service;

import com.kotomka.core.dao.AdminCoreDAO;
import com.kotomka.core.exceptions.AuthException;
import com.kotomka.core.model.server.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminCoreDAO adminCoreDAO;

    @Autowired
    public AuthService(AdminCoreDAO adminCoreDAO) {
        this.adminCoreDAO = adminCoreDAO;
    }

    public UserModel checkSystemUser(final String login, final String password) throws AuthException {
        return adminCoreDAO.checkSystemUser(login, password);
    }
}