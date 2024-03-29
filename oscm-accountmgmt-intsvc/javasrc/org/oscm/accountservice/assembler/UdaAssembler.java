/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Author: weiser                                                    
 *                                                                              
 *  Creation Date: 12.10.2010                                                      
 *                                                                              
 *  Completion Time: 13.10.2010                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.assembler;

import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UdaDefinition;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.validator.BLValidator;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * @author weiser
 * 
 */
public class UdaAssembler extends BaseAssembler {

    private UdaAssembler() {

    }

    public static UdaDefinition toUdaDefinition(VOUdaDefinition voUdaDefinition)
            throws ValidationException {
        if (voUdaDefinition == null) {
            return null;
        }
        UdaDefinition udaDefinition = new UdaDefinition();
        copyDefinitionAttributes(voUdaDefinition, udaDefinition);
        return udaDefinition;
    }

    public static UdaDefinition updateUdaDefinition(
            UdaDefinition udaDefinition, VOUdaDefinition voUdaDefinition)
            throws ValidationException, ConcurrentModificationException {
        verifyVersionAndKey(udaDefinition, voUdaDefinition);
        copyDefinitionAttributes(voUdaDefinition, udaDefinition);
        return udaDefinition;
    }

    public static VOUdaDefinition toVOUdaDefinition(UdaDefinition udaDefinition) {
        if (udaDefinition == null) {
            return null;
        }
        VOUdaDefinition voUdaDefinition = new VOUdaDefinition();
        updateValueObject(voUdaDefinition, udaDefinition);
        voUdaDefinition.setDefaultValue(udaDefinition.getDefaultValue());
        voUdaDefinition.setTargetType(udaDefinition.getTargetType().name());
        voUdaDefinition.setUdaId(udaDefinition.getUdaId());
        voUdaDefinition.setConfigurationType(udaDefinition
                .getConfigurationType());
        return voUdaDefinition;
    }

    public static Uda toUda(VOUda voUda) throws ValidationException {
        if (voUda == null) {
            return null;
        }
        Uda uda = new Uda();
        copyAttributes(voUda, uda);
        return uda;
    }

    public static Uda updateUda(Uda uda, VOUda voUda)
            throws ValidationException, ConcurrentModificationException {
        verifyVersionAndKey(uda, voUda);
        copyAttributes(voUda, uda);
        return uda;
    }

    public static VOUda toVOUda(Uda uda) {
        if (uda == null) {
            return null;
        }
        VOUda voUda = new VOUda();
        updateValueObject(voUda, uda);
        voUda.setTargetObjectKey(uda.getTargetObjectKey());
        voUda.setUdaDefinition(toVOUdaDefinition(uda.getUdaDefinition()));
        voUda.setUdaValue(uda.getUdaValue());
        return voUda;
    }

    private static void copyDefinitionAttributes(
            VOUdaDefinition voUdaDefinition, UdaDefinition udaDefinition)
            throws ValidationException {
        validateDefinition(voUdaDefinition);
        udaDefinition.setDefaultValue(voUdaDefinition.getDefaultValue());
        udaDefinition.setTargetType(UdaTargetType.valueOf(voUdaDefinition
                .getTargetType()));
        udaDefinition.setUdaId(voUdaDefinition.getUdaId());
        udaDefinition.setConfigurationType(voUdaDefinition
                .getConfigurationType());
    }

    private static void validateDefinition(VOUdaDefinition def)
            throws ValidationException {
        BLValidator.isId("udaId", def.getUdaId(), true);
        BLValidator.isDescription("defaultValue", def.getDefaultValue(), false);
        String targetType = def.getTargetType();
        toUdaTargetType(targetType);
        BLValidator.isNotNull("configurationType", def.getConfigurationType());
    }

    public static UdaTargetType toUdaTargetType(String targetType)
            throws ValidationException {
        BLValidator.isNotBlank("targetType", targetType);
        try {
            return UdaTargetType.valueOf(targetType);
        } catch (IllegalArgumentException e) {
            ValidationException ex = new ValidationException(
                    ReasonEnum.INVALID_REFERENCE, "targetType",
                    new Object[] { targetType });
            throw ex;
        }
    }

    private static Uda copyAttributes(VOUda voUda, Uda uda)
            throws ValidationException {
        validate(voUda);
        uda.setTargetObjectKey(voUda.getTargetObjectKey());
        uda.setUdaValue(voUda.getUdaValue());
        return uda;
    }

    public static void validate(VOUda uda) throws ValidationException {
        BLValidator.isNotNull("udaDefinition", uda.getUdaDefinition());
        validateDefinition(uda.getUdaDefinition());
        BLValidator.isInRange("targetObjectKey", uda.getTargetObjectKey(),
                Long.valueOf(1), null);
        BLValidator.isDescription("udaValue", uda.getUdaValue(), false);
    }
}
