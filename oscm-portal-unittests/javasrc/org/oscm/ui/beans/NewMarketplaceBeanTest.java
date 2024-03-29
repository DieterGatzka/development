/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2015 
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.faces.application.FacesMessage.Severity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.model.NewMarketplace;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.vo.VOMarketplace;

@SuppressWarnings("boxing")
public class NewMarketplaceBeanTest {

    NewMarketplaceBean nmpb;
    private MenuBean mb;
    private MarketplaceService mps;

    @Before
    public void setup() throws Exception {
        nmpb = spy(new NewMarketplaceBean());
        mb = mock(MenuBean.class);
        mps = mock(MarketplaceService.class);

        doReturn(mps).when(nmpb).getMarketplaceService();
        doNothing().when(nmpb).addMessage(anyString(), any(Severity.class),
                anyString(), anyString());

        doAnswer(new Answer<VOMarketplace>() {

            @Override
            public VOMarketplace answer(InvocationOnMock invocation)
                    throws Throwable {
                VOMarketplace mp = (VOMarketplace) invocation.getArguments()[0];
                mp.setMarketplaceId("marketplaceId");
                return mp;
            }
        }).when(mps).createMarketplace(any(VOMarketplace.class));

        nmpb.menuBean = mb;
    }

    @Test
    public void getModel() {
        assertNotNull(nmpb.getModel());
    }

    @Test
    public void getModel_twice() {
        NewMarketplace m1 = nmpb.getModel();
        NewMarketplace m2 = nmpb.getModel();
        assertSame(m1, m2);
    }

    @Test
    public void toVOMarketplace() {
        NewMarketplace nmp = new NewMarketplace();
        nmp.setName("newmarketplace");
        nmp.setClosed(false);
        nmp.setOwningOrganizationId("new marketplace id");
        nmp.setTaggingEnabled(false);
        nmp.setReviewEnabled(false);
        nmp.setSocialBookmarkEnabled(true);
        nmp.setCategoriesEnabled(false);

        VOMarketplace vmp = nmpb.toVOMarketplace(nmp);

        assertEquals(nmp.getName(), vmp.getName());
        assertEquals(nmp.getOwningOrganizationId(),
                nmp.getOwningOrganizationId());

        assertEquals(!nmp.isClosed(), vmp.isOpen());
        assertEquals(nmp.isTaggingEnabled(), vmp.isTaggingEnabled());
        assertEquals(nmp.isReviewEnabled(), vmp.isReviewEnabled());
        assertEquals(nmp.isSocialBookmarkEnabled(),
                vmp.isSocialBookmarkEnabled());
        assertEquals(nmp.isCategoriesEnabled(), vmp.isCategoriesEnabled());

    }

    @Test
    public void toVOMarketplace_null() {
        VOMarketplace vmp = nmpb.toVOMarketplace(null);
        assertNull(vmp);
    }

    @Test
    public void createMarketplace() throws Exception {
        NewMarketplace model1 = nmpb.getModel();
        String token = nmpb.getToken();
        nmpb.setToken(token);
        nmpb.createMarketplace();
        ArgumentCaptor<VOMarketplace> ac = ArgumentCaptor
                .forClass(VOMarketplace.class);
        verify(mps, times(1)).createMarketplace(ac.capture());

        verify(mb, times(1)).resetMenuVisibility();
        VOMarketplace value = ac.getValue();
        assertNotNull(value);
        NewMarketplace model2 = nmpb.getModel();

        // make sure the old model is not available any more
        assertNotSame(model1, model2);

    }

    @Test
    public void createMarketplace_invalidToken() throws Exception {

        nmpb.createMarketplace();
        verifyNoMoreInteractions(mps, mb);
    }
}
