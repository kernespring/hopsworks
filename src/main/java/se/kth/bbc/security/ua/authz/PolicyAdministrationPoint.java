/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.bbc.security.ua.authz;

import javax.ejb.EJB;
import se.kth.bbc.security.ua.BBCGroup;
import se.kth.bbc.security.ua.UserManager;
import se.kth.bbc.security.ua.model.User;

/**
 *
 * @author Ali Gholami <gholami@pdc.kth.se>
 */
public class PolicyAdministrationPoint {

  @EJB
  private UserManager userPolicMgr;

  public boolean isInAdminRole(User user) {
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.SYS_ADMIN.
            name());
  }

  public boolean isInDataProviderRole(User user) {
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.BBC_ADMIN.
            name());
  }

  public boolean isInAuditorRole(User user) {
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.AUDITOR.
            name());
  }

  public boolean isInResearcherRole(User user) {
    return userPolicMgr.findGroups(user.getUid()).contains(
            BBCGroup.BBC_RESEARCHER.name());
  }

  public boolean isInGuestRole(User user) {
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.BBC_GUEST.
            name());
  }

  public boolean isInAdminRole(String username) {
    User user = userPolicMgr.getUserByUsernmae(username);
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.SYS_ADMIN.
            name());
  }

  public boolean isInResearcherRole(String username) {
    User user = userPolicMgr.getUserByUsernmae(username);
    return userPolicMgr.findGroups(user.getUid()).contains(
            BBCGroup.BBC_RESEARCHER.name());
  }

  public boolean isInDataProviderRole(String username) {
    User user = userPolicMgr.getUserByUsernmae(username);
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.BBC_ADMIN.
            name());
  }

  public boolean isInAuditorRole(String username) {
    User user = userPolicMgr.getUserByUsernmae(username);
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.AUDITOR.
            name());
  }

  public boolean isInGuestRole(String username) {
    User user = userPolicMgr.getUserByUsernmae(username);
    return userPolicMgr.findGroups(user.getUid()).contains(BBCGroup.BBC_GUEST.
            name());
  }

  public String redirectUser(User user) {

    if (isInAdminRole(user)) {
      return "adminIndex";
    } else if (isInAuditorRole(user)) {
      return "auditIndex";
    } else if (isInDataProviderRole(user)) {
      return "indexPage";
    } else if (isInResearcherRole(user)) {
      return "indexPage";
    }

    return "indexPage";
  }
}
