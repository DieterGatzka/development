/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                        
 *       
 *  Creation Date: 2011-05-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCountryVatRate;
import org.oscm.vo.VOOrganizationVatRate;
import org.oscm.vo.VOVatRate;

/**
 * Remote interface for handling VAT-related tasks.
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface VatService {

    /**
     * Saves the default VAT rate for the calling user's organization.
     * <p>
     * If the VAT support is disabled and the given parameter is not
     * <code>null</code>, the VAT support is enabled. If the VAT support is
     * enabled and the given parameter is <code>null</code>, all existing VAT
     * rates are removed and the VAT support is disabled.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param defaultVat
     *            the default VAT rate
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ConcurrentModificationException
     *             if the stored VAT rate is changed by another user in the time
     *             between reading and writing it
     * @throws ValidationException
     *             if the validation of the VAT rate fails
     */
    @WebMethod
    public void saveDefaultVat(
            @WebParam(name = "defaultVat") VOVatRate defaultVat)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException;

    /**
     * Saves country-specific VAT rates for the calling user's organization. To
     * delete an existing country-specific VAT rate, add the corresponding value
     * object to the list and set its rate to <code>null</code>.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param countryVats
     *            the list of country-specific VAT rates
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ConcurrentModificationException
     *             if a stored VAT rate is changed by another user in the time
     *             between reading and writing it
     * @throws ValidationException
     *             if the validation of a VAT rate fails, or if the VAT support
     *             is disabled for the calling user's organization
     */
    @WebMethod
    public void saveCountryVats(
            @WebParam(name = "countryVats") List<VOCountryVatRate> countryVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, ValidationException;

    /**
     * Saves organization-specific VAT rates for the calling user's
     * organization. To delete an existing organization-specific VAT rate, add
     * the corresponding value object to the list and set its rate to
     * <code>null</code>.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param organizationVats
     *            the list of organization-specific VAT rates
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ConcurrentModificationException
     *             if a stored VAT rate is changed by another user in the time
     *             between reading and writing it
     * @throws OperationNotPermittedException
     *             if an organization specified in the VAT rates is not a
     *             customer of the calling user's organization
     * @throws ValidationException
     *             if the validation of a VAT rate fails, or if the VAT support
     *             is disabled for the calling user's organization
     */
    @WebMethod
    public void saveOrganizationVats(
            @WebParam(name = "organizationVats") List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException;

    /**
     * Saves the default, country-specific, and organization-specific VAT rates
     * for the calling user's organization. If the default VAT rate is
     * <code>null</code>, the lists of country-specific and
     * organization-specific VAT rates must be <code>null</code> or empty.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param defaultVat
     *            the default VAT rate
     * @param countryVats
     *            a list of country-specific VAT rates
     * @param organizationVats
     *            a list of organization-specific VAT rates
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ConcurrentModificationException
     *             if a stored VAT rate is changed by another user in the time
     *             between reading and writing it
     * @throws OperationNotPermittedException
     *             if an organization specified in the organization-specific VAT
     *             rates is not a customer of the calling user's organization
     * @throws ValidationException
     *             if the validation of a VAT rate fails
     */
    public void saveAllVats(
            @WebParam(name = "defaultVat") VOVatRate defaultVat,
            @WebParam(name = "countryVats") List<VOCountryVatRate> countryVats,
            @WebParam(name = "organizationVats") List<VOOrganizationVatRate> organizationVats)
            throws OrganizationAuthoritiesException,
            ConcurrentModificationException, OperationNotPermittedException,
            ValidationException;

    /**
     * Retrieves the default VAT rate set for the calling user's organization.
     * If the VAT support is disabled, <code>null</code> is returned.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the default VAT rate, or <code>null</code> if the VAT support is
     *         disabled
     */
    public VOVatRate getDefaultVat();

    /**
     * Retrieves the country-specific VAT rates set for the calling user's
     * organization. If the VAT support is disabled, an empty list is returned.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the country-specific VAT rates
     */
    public List<VOCountryVatRate> getCountryVats();

    /**
     * Retrieves the organization-specific VAT rates set for the calling user's
     * organization. If the VAT support is disabled, an empty list is returned.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the organization-specific VAT rates
     */
    public List<VOOrganizationVatRate> getOrganizationVats();

    /**
     * Checks whether the VAT support is enabled for the calling user's
     * organization.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return <code>true</code> if the VAT support is enabled,
     *         <code>false</code> otherwise
     */
    public boolean getVatSupport();

}
