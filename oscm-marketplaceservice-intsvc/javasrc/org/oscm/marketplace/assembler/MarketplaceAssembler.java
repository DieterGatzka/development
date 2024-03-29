/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Creation Date: 08.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.assembler;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOMarketplace;

/**
 * Assembler to handle VOMarketplace <=> Marketplace conversions.
 * 
 * @author groch
 * 
 */
public class MarketplaceAssembler extends BaseAssembler {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(MarketplaceAssembler.class);

    /**
     * Creates a value object representing the current settings for the
     * marketplace.
     * 
     * @param domObj
     *            The technical marketplace to be represented as value object.
     * @param facade
     *            The localizer facade object.
     * @return A value object representation of the given marketplace.
     */
    public static VOMarketplace toVOMarketplace(Marketplace domObj,
            LocalizerFacade facade) {
        if (domObj == null) {
            return null;
        }
        VOMarketplace voResult = new VOMarketplace();
        updateValueObject(voResult, domObj);
        voResult.setMarketplaceId(domObj.getMarketplaceId());
        voResult.setName(facade.getText(domObj.getKey(),
                LocalizedObjectTypes.MARKETPLACE_NAME));
        voResult.setOpen(domObj.isOpen());

        voResult.setTaggingEnabled(domObj.isTaggingEnabled());
        voResult.setReviewEnabled(domObj.isReviewEnabled());
        voResult.setSocialBookmarkEnabled(domObj.isSocialBookmarkEnabled());
        voResult.setCategoriesEnabled(domObj.isCategoriesEnabled());

        Organization owner = domObj.getOrganization();
        if (owner != null) {
            voResult.setOwningOrganizationId(owner.getOrganizationId());
            String name = owner.getName();
            // FIXME what is "<empty>" good for?
            voResult.setOwningOrganizationName(name != null ? name : "<empty>");
        }
        return voResult;
    }

    /**
     * Updates the fields in the Marketplace object to reflect the changes
     * performed in the value object.
     * 
     * @param domObj
     *            The domain object to be updated.
     * @param voObj
     *            The value object.
     * @return The updated domain object.
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions do not match.
     * @throws ValidationException
     *             Thrown if the attributes to copy at the value object do not
     *             meet all constraints.
     */
    public static Marketplace updateMarketplace(Marketplace domObj,
            VOMarketplace voObj) throws ValidationException,
            ConcurrentModificationException {
        if (domObj == null || voObj == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Parameters must not be null");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_PARAMETER_NULL);
            throw e;
        }
        if (domObj.getKey() != 0) {
            verifyVersionAndKey(domObj, voObj);
        }
        validate(voObj);
        copyAttributes(domObj, voObj);
        return domObj;
    }

    public static Marketplace toMarketplace(VOMarketplace voObj)
            throws ValidationException {
        final Marketplace domObj = new Marketplace();
        validate(voObj);
        copyAttributes(domObj, voObj);
        return domObj;
    }

    public static void validate(VOMarketplace voObj) throws ValidationException {
        BLValidator.isId("marketplaceId", voObj.getMarketplaceId(), true);
    }

    private static void copyAttributes(Marketplace domObj, VOMarketplace voObj) {
        domObj.setMarketplaceId(voObj.getMarketplaceId());
        domObj.setOpen(voObj.isOpen());

        domObj.setTaggingEnabled(voObj.isTaggingEnabled());
        domObj.setReviewEnabled(voObj.isReviewEnabled());
        domObj.setSocialBookmarkEnabled(voObj.isSocialBookmarkEnabled());
        domObj.setCategoriesEnabled(voObj.isCategoriesEnabled());
    }
}
