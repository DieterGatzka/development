/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.test.stubs;

import java.util.List;

import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAlreadyBannedException;
import org.oscm.internal.types.exception.OrganizationAlreadyExistsException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

public class MarketplaceServiceStub implements MarketplaceService {

    @Override
    public List<VOMarketplace> getMarketplacesForOrganization() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOCatalogEntry> getMarketplacesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOServiceDetails publishService(VOService service,
            List<VOCatalogEntry> entries) throws ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException,
            PublishingToMarketplaceNotPermittedException {
        throw new UnsupportedOperationException();

    }

    @Override
    public VOMarketplace getMarketplaceForSubscription(long subscriptionKey,
            String locale) throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getMarketplacesOwned() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOMarketplace> getMarketplacesForOperator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOMarketplace updateMarketplace(VOMarketplace marketplace)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException {
        throw new UnsupportedOperationException();

    }

    @Override
    public VOMarketplace createMarketplace(VOMarketplace marketplace)
            throws OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteMarketplace(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addOrganizationsToMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException,
            OrganizationAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeOrganizationsFromMarketplace(
            List<String> organizationIds, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getOrganizationsForMarketplace(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VOMarketplace getMarketplaceById(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void banOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationAuthorityException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAlreadyBannedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void liftBanOrganizationsFromMarketplace(
            List<String> organizationIds, String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VOOrganization> getBannedOrganizationsForMarketplace(
            String marketplaceId) throws ObjectNotFoundException,
            OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBrandingUrl(String marketplaceId)
            throws ObjectNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveBrandingUrl(VOMarketplace marketplace, String brandingUrl)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

}
