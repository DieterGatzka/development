/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2015                                           
 *                                                                                                                                  
 *  Creation Date: 20.07.15 14:05
 *
 *******************************************************************************/

package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserGroup;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.Converter;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.ExceptionConverter;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.intf.OrganizationalUnitService;
import org.oscm.pagination.Pagination;
import org.oscm.types.enumtypes.UnitRoleType;
import org.oscm.types.exceptions.DeletionConstraintException;
import org.oscm.types.exceptions.DomainObjectException;
import org.oscm.types.exceptions.MailOperationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.vo.VOOrganizationalUnit;
import org.oscm.vo.VOUser;

/**
 * Endpoint facade for {@link OrganizationalUnitService}
 */
@WebService(endpointInterface = "org.oscm.intf.OrganizationalUnitService")
public class OrganizationalUnitServiceWS implements OrganizationalUnitService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(OrganizationalUnitServiceWS.class));

    UserGroupServiceLocalBean localService;
    DataService dataService;
    WebServiceContext wsContext;

    @Override
    public void grantUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, dataService);

        PlatformUser pUser = Converter.convert(user, VOUser.class,
                PlatformUser.class);
        UserGroup group = Converter.convert(organizationalUnit,
                VOOrganizationalUnit.class, UserGroup.class);

        try {
            localService
                    .grantUserRoles(
                            pUser,
                            EnumConverter
                                    .convertList(
                                            roles,
                                            org.oscm.internal.types.enumtypes.UnitRoleType.class),
                            group);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void revokeUserRoles(VOUser user, List<UnitRoleType> roles,
            VOOrganizationalUnit organizationalUnit)
            throws ObjectNotFoundException, OperationNotPermittedException {
        WS_LOGGER.logAccess(wsContext, dataService);

        PlatformUser pUser = Converter.convert(user, VOUser.class,
                PlatformUser.class);
        UserGroup group = Converter.convert(organizationalUnit,
                VOOrganizationalUnit.class, UserGroup.class);

        try {
            localService
                    .revokeUserRoles(
                            pUser,
                            EnumConverter
                                    .convertList(
                                            roles,
                                            org.oscm.internal.types.enumtypes.UnitRoleType.class),
                            group);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public List<VOOrganizationalUnit> getOrganizationalUnits(
            Pagination pagination) {
        WS_LOGGER.logAccess(wsContext, dataService);

        List<UserGroup> units = localService.getOrganizationalUnits(Converter
                .convert(pagination, Pagination.class,
                        org.oscm.pagination.Pagination.class));

        return Converter.convertList(units, UserGroup.class,
                VOOrganizationalUnit.class);
    }

    @Override
    public VOOrganizationalUnit createUnit(String unitName, String description,
            String referenceId) throws NonUniqueBusinessKeyException {
        WS_LOGGER.logAccess(wsContext, dataService);

        try {
            UserGroup unit = localService.createUserGroup(unitName,
                    description, referenceId);

            return Converter.convert(unit, UserGroup.class,
                    VOOrganizationalUnit.class);
        } catch (org.oscm.internal.types.exception.NonUniqueBusinessKeyException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }

    @Override
    public void deleteUnit(String organizationalUnitName)
            throws ObjectNotFoundException, OperationNotPermittedException,
            DeletionConstraintException, MailOperationException {
        WS_LOGGER.logAccess(wsContext, dataService);

        try {
            localService.deleteUserGroup(organizationalUnitName);
        } catch (org.oscm.internal.types.exception.ObjectNotFoundException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (org.oscm.internal.types.exception.OperationNotPermittedException e) {
            throw ExceptionConverter.convertToApi(e);
        } catch (DeletingUnitWithSubscriptionsNotPermittedException e) {
            throw new DeletionConstraintException(
                    DomainObjectException.ClassEnum.USER_GROUP,
                    organizationalUnitName,
                    DomainObjectException.ClassEnum.SUBSCRIPTION);
        } catch (org.oscm.internal.types.exception.MailOperationException e) {
            throw ExceptionConverter.convertToApi(e);
        }
    }
}
