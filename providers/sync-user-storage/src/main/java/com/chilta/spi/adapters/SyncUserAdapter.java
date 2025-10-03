package com.chilta.spi.adapters;

import com.chilta.spi.exceptions.DatabaseErrorException;
import com.chilta.spi.managers.DatabaseConnectionManager;

import org.keycloak.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class SyncUserAdapter implements UserModel {
    private static final Logger logger = LoggerFactory.getLogger(SyncUserAdapter.class);

    private final UserModel delegate;
    private final DatabaseConnectionManager databaseConnectionManager;
    private String tempFirstName;
    private String tempLastName;

    public SyncUserAdapter(UserModel delegate, DatabaseConnectionManager databaseConnectionManager) {
        this.delegate = delegate;
        this.databaseConnectionManager = databaseConnectionManager;
    }

    @Override
    public void setEmail(String email) {
        Connection connection = databaseConnectionManager.getConnection();
        String sql = "UPDATE users SET \"updatedAt\" = NOW(), email = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(delegate.getId());
            statement.setObject(2, uuid);
            statement.setString(1, email);
            int rowsAffected = statement.executeUpdate();
            logger.info("Updated user email in backend: {} rows affected", rowsAffected);
        } catch (SQLException e) {
            throw new DatabaseErrorException("Error while updating email.", e);
        }
        delegate.setEmail(email);
    }

    @Override
    public boolean isEmailVerified() {
        return delegate.isEmailVerified();
    }

    @Override
    public void setEmailVerified(boolean verified) {
        delegate.setEmailVerified(verified);
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        return delegate.getGroupsStream();
    }

    @Override
    public void joinGroup(GroupModel group) {
        delegate.joinGroup(group);
    }

    @Override
    public void leaveGroup(GroupModel group) {
        delegate.leaveGroup(group);
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return delegate.isMemberOf(group);
    }

    @Override
    public String getFederationLink() {
        return delegate.getFederationLink();
    }

    @Override
    public void setFederationLink(String link) {
        delegate.setFederationLink(link);
    }

    @Override
    public String getServiceAccountClientLink() {
        return delegate.getServiceAccountClientLink();
    }

    @Override
    public void setServiceAccountClientLink(String clientInternalId) {
        delegate.setServiceAccountClientLink(clientInternalId);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return delegate.credentialManager();
    }

    @Override
    public void setFirstName(String firstName) {
        tempFirstName = firstName;
        Connection connection = databaseConnectionManager.getConnection();
        String sql = "UPDATE users SET \"updatedAt\" = NOW(), \"pictureLink\" = ?, \"firstName\" = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(delegate.getId());
            statement.setObject(3, uuid);
            statement.setString(2, firstName);
            statement.setString(1, getPictureLink(this));
            int rowsAffected = statement.executeUpdate();
            logger.info("Updated user firstName in backend: {} rows affected", rowsAffected);
        } catch (SQLException e) {
            throw new DatabaseErrorException("Error while updating firstName.", e);
        }
        delegate.setFirstName(firstName);
    }

    @Override
    public void setLastName(String lastName) {
        tempLastName = lastName;
        Connection connection = databaseConnectionManager.getConnection();
        String sql = "UPDATE users SET \"updatedAt\" = NOW(), \"pictureLink\" = ?, \"lastName\" = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(delegate.getId());
            statement.setObject(3, uuid);
            statement.setString(2, lastName);
            statement.setString(1, getPictureLink(this));
            int rowsAffected = statement.executeUpdate();
            logger.info("Updated user email in lastName: {} rows affected", rowsAffected);
        } catch (SQLException e) {
            throw new DatabaseErrorException("Error while updating lastName.", e);
        }
        delegate.setLastName(lastName);
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public void setUsername(String username) {
        Connection connection = databaseConnectionManager.getConnection();
        String sql = "UPDATE users SET \"updatedAt\" = NOW(), username = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(delegate.getId());
            statement.setObject(2, uuid);
            statement.setString(1, username);
            int rowsAffected = statement.executeUpdate();
            logger.info("Updated user email in username: {} rows affected", rowsAffected);
        } catch (SQLException e) {
            throw new DatabaseErrorException("Error while updating username.", e);
        }
        delegate.setUsername(username);
    }

    @Override
    public Long getCreatedTimestamp() {
        return delegate.getCreatedTimestamp();
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        delegate.setCreatedTimestamp(timestamp);
    }

    @Override
    public String getEmail() {
        return delegate.getEmail();
    }

    @Override
    public String getFirstName() {
        if (tempFirstName == null) {
            return delegate.getFirstName();
        }
        return tempFirstName;
    }

    @Override
    public String getLastName() {
        if (tempLastName == null) {
            return delegate.getLastName();
        }
        return tempLastName;
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        delegate.setEnabled(enabled);
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        delegate.setSingleAttribute(name, value);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        switch (name) {
            case UserModel.FIRST_NAME:
                setFirstName(values.get(0));
                break;
            case UserModel.LAST_NAME:
                setLastName(values.get(0));
                break;
            case UserModel.EMAIL:
                setEmail(values.get(0));
                break;
        }
        delegate.setAttribute(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        delegate.removeAttribute(name);
    }

    @Override
    public String getFirstAttribute(String name) {
        return delegate.getFirstAttribute(name);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return delegate.getRequiredActionsStream();
    }

    @Override
    public void addRequiredAction(String action) {
        delegate.addRequiredAction(action);
    }

    @Override
    public void removeRequiredAction(String action) {
        delegate.removeRequiredAction(action);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return delegate.getAttributeStream(name);
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return delegate.getRealmRoleMappingsStream();
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return delegate.getClientRoleMappingsStream(app);
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return delegate.hasRole(role);
    }

    @Override
    public void grantRole(RoleModel role) {
        delegate.grantRole(role);
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return delegate.getRoleMappingsStream();
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        delegate.deleteRoleMapping(role);
    }

    public static String getPictureLink(UserModel user) {
        String pictureLink = user.getFirstAttribute("picture");
        if (pictureLink == null || pictureLink.isEmpty()) {
            return "https://ui-avatars.com/api/?name=" +
                    user.getFirstName() + "+" + user.getLastName() +
                    "&background=random";
        }
        return pictureLink;
    }
}