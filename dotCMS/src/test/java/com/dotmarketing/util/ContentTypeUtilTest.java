package com.dotmarketing.util;

import static com.dotcms.util.CollectionsUtils.list;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.dotcms.UnitTestBase;
import com.dotcms.api.web.HttpServletRequestThreadLocal;
import com.dotcms.cms.login.LoginServiceAPI;
import com.dotcms.contenttype.transform.contenttype.StructureTransformer;
import com.dotcms.util.ContentTypeUtil;
import com.dotmarketing.business.Layout;
import com.dotmarketing.business.LayoutAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.languagesmanager.business.LanguageAPI;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.portlets.structure.model.Structure;
import com.liferay.portal.model.User;
import com.liferay.portal.util.WebKeys;

/**
 * Ensures the correct behavior of the {@link ContentTypeUtil} class.
 * 
 * @author Freddy Rodriguez
 */
public class ContentTypeUtilTest extends UnitTestBase {

    @Test
    public void testGetActionUrl() throws DotDataException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        LayoutAPI layoutAPI = mock(LayoutAPI.class);
        LoginServiceAPI loginService = mock(LoginServiceAPI.class);
        LanguageAPI languageAPI = mock(LanguageAPI.class);
        User user = mock(User.class);
        Structure structure = mock(Structure.class);
        Language language = mock(Language.class);
        HttpServletRequestThreadLocal httpServletRequestThreadLocal = mock(HttpServletRequestThreadLocal.class);

        ContentTypeUtil contentTypeUtil = new ContentTypeUtil(layoutAPI, languageAPI,
                httpServletRequestThreadLocal, loginService);

        Layout layout = new Layout();
        layout.setPortletIds(list(PortletID.CONTENT.toString()));
        final String layoutId = "71b8a1ca-37b6-4b6e-a43b-c7482f28db6c";
        layout.setId(layoutId);
        List<Layout> layouts = list(layout);

        when(structure.getStructureType()).thenReturn(1);
        final String contentTypeInode = "38a3f133-85e1-4b07-b55e-179f38303b90";
        when(structure.getInode()).thenReturn(contentTypeInode);
        when(structure.getModDate()).thenReturn(new Date());
        when(structure.getIDate()).thenReturn(new Date());
        when(structure.getName()).thenReturn("testSt");
        when(structure.getVelocityVarName()).thenReturn("testSt");
        when(layoutAPI.loadLayoutsForUser(user)).thenReturn(layouts);
        when(request.getSession()).thenReturn(session);
        when(request.getServerName()).thenReturn("localhost");
        when(session.getAttribute(WebKeys.CTX_PATH)).thenReturn("/ctx");
        when(session.getAttribute(com.dotmarketing.util.WebKeys.CMS_USER)).thenReturn(user);
        when(languageAPI.getLanguage("en", "US")).thenReturn(language);
        final long languageId = 1l;
        when(language.getId()).thenReturn(languageId);
        when(httpServletRequestThreadLocal.getRequest()).thenReturn(request);
        when(loginService.getLoggedInUser(request)).thenReturn(user);

        String expected = "/ctx/portal_public/layout?p_l_id=" + layoutId + "&p_p_id=" + PortletID.CONTENT.toString() + "&p_p_action=1&p_p_state=maximized&_content_inode=&_content_referer=%2Fctx%2Fportal_public%2Flayout%3Fp_l_id%3D" + layout.getId() + "%26p_p_id%3D" + PortletID.CONTENT.toString() + "%26p_p_action%3D1%26p_p_state%3Dmaximized%26_content_inode%3D%26_content_structure_id%3D" + contentTypeInode + "%26_content_cmd%3Dnew%26_content_lang%3D" + languageId + "%26_content_struts_action%3D%252Fext%252Fcontentlet%252Fview_contentlets&_content_selectedStructure=" + contentTypeInode + "&_content_cmd=new&_content_lang=" + languageId + "&_content_struts_action=%2Fext%2Fcontentlet%2Fedit_contentlet";

        String actionUrl = contentTypeUtil.getActionUrl(new StructureTransformer(structure).from());
        System.out.println("actionUrl = " + actionUrl);
        assertEquals("The expected actionUrl is not the same as the one generated by the Util.", expected, actionUrl);
    }

}
