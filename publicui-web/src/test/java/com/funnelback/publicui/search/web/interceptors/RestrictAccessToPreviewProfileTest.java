package com.funnelback.publicui.search.web.interceptors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import static com.funnelback.config.keys.Keys.FrontEndKeys;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.exception.InvalidCollectionException;
import com.funnelback.publicui.search.web.interceptors.helpers.IntercepterHelper;
import com.funnelback.publicui.utils.web.ExecutionContextHolder;
import com.funnelback.publicui.utils.web.ProfilePicker;
import com.funnelback.springmvc.api.config.security.user.model.FunnelbackUser;
import com.funnelback.springmvc.api.config.security.user.model.RestrictionSet;
import com.funnelback.springmvc.api.config.security.user.model.UserInfoDetails;
import com.funnelback.springmvc.web.security.CurrentFunnelbackUserHelper;

import lombok.SneakyThrows;
public class RestrictAccessToPreviewProfileTest {

    @Test
    public void testHasAccessToCollection() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Admin));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(true, true));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertTrue(wasGrantedAccess);
    }
    
    @Test
    public void testNoAccessToCollection() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Admin));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, true)); // user does not have access to coll
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertFalse("When preview is restricted and user does not have permission to the collection then they should not be given access", wasGrantedAccess);
    }
    
    @Test
    public void testNoAccessToProfile() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Admin));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(true, false)); // user does not have access to profile
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertFalse("When preview is restricted and user does not have permission to the collection then they should not be given access", wasGrantedAccess);
    }
    
    
    @Test
    public void testNotInAdminExecutionContext() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public)); // not in admin context
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(true, true));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertFalse("When preview is restricted it should not be accessible in the Public context", wasGrantedAccess);
    }
    
    @Test
    public void testNonPreviewProfile() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, false));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview_not")); // profile is not a preview one
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertTrue("Profile is not a preview profile so access is granted.", wasGrantedAccess);
    }
    
    @Test
    public void testRestrictionsNotEnabled() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(false))); // access allowed to preview
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, false));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertTrue("preview profile has no restrictions so all users can access it no matter the context", wasGrantedAccess);
    }
    
    @Test
    public void testMissingProfile() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), null)); // unknown profile will throw exception
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, false));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        try {
            restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
            Assert.fail("Should have thrown an exception when no valid profile could be found");
        } catch (InvalidCollectionException e) {
            
        }
        
    }
    
    @Test
    public void testCollectionObjectIsNull() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(null, mockServiceConfigWithRestriction(true))); // Collection is null
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public)); 
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, false));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper("collection"));
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertTrue("Allow access for non existent collections", wasGrantedAccess);
    }
    
    @Test
    public void testCollectionIdFromRequestisNull() throws Exception {
        RestrictAccessToPreviewProfile restrictAccessToPreviewProfile = new RestrictAccessToPreviewProfile();
        restrictAccessToPreviewProfile.setI18n(mockI18n());
        restrictAccessToPreviewProfile.setConfigRepository(mockConfigRepistory(mock(Collection.class), mockServiceConfigWithRestriction(true)));
        restrictAccessToPreviewProfile.setExecutionContextHolder(mockExecutionContextHolder(ExecutionContext.Public));
        restrictAccessToPreviewProfile.setCurrentFunnelbackUserHelper(mockUserWithAccessToCollectionProfile(false, false));
        restrictAccessToPreviewProfile.setIntercepterHelper(mockIntercepterHelper(null)); // collection id from ?cgi params is null
        restrictAccessToPreviewProfile.setProfilePicker(mockProfilePicker("_default_preview"));
        
        boolean wasGrantedAccess = restrictAccessToPreviewProfile.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), null);
        Assert.assertTrue("Allow access if no collection is set", wasGrantedAccess);
    }
    
    
    
    
    private IntercepterHelper mockIntercepterHelper(String collection) {
        IntercepterHelper intercepterHelper = mock(IntercepterHelper.class);
        when(intercepterHelper.getCollectionFromRequest(any())).thenReturn(collection);
        return intercepterHelper;
    }
    
    @SneakyThrows
    private ConfigRepository mockConfigRepistory(Collection collection, ServiceConfigReadOnly serviceConfig) {
        ConfigRepository configRepository = mock(ConfigRepository.class);
        when(configRepository.getCollection(anyString())).thenReturn(collection);
        if(serviceConfig != null) {
            when(configRepository.getServiceConfig(anyString(), anyString())).thenReturn(serviceConfig);
        } else {
            when(configRepository.getServiceConfig(anyString(), anyString())).thenThrow(new ProfileNotFoundException("c", "p"));
        }
        return configRepository;
    }
    
    private ProfilePicker mockProfilePicker(String profile) {
        ProfilePicker profilePicker = mock(ProfilePicker.class);
        when(profilePicker.existingProfileForCollection(any(), any())).thenReturn(profile);
        return profilePicker;
    }
    
    private ServiceConfigReadOnly mockServiceConfigWithRestriction(boolean previewRestrictionEnabled) {
        ServiceConfigReadOnly serviceConfigReadOnly = mock(ServiceConfigReadOnly.class);
        when(serviceConfigReadOnly.get(FrontEndKeys.RESTRICT_PREVIEW_TO_AUTHENTICATED_USERS)).thenReturn(previewRestrictionEnabled);
        return serviceConfigReadOnly;
    }
    
    private ExecutionContextHolder mockExecutionContextHolder(ExecutionContext executionContext) {
        ExecutionContextHolder executionContextHolder = mock(ExecutionContextHolder.class);
        when(executionContextHolder.getExecutionContext()).thenReturn(executionContext);
        return executionContextHolder;
    }
    
    private CurrentFunnelbackUserHelper mockUserWithAccessToCollectionProfile(boolean hasAccess, boolean hasAccessToprofile) {
        CurrentFunnelbackUserHelper currentFunnelbackUserHelper = mock(CurrentFunnelbackUserHelper.class);
        
        FunnelbackUser user = mock(FunnelbackUser.class);
        UserInfoDetails userDetails = mock(UserInfoDetails.class);
        
        RestrictionSet restrictionSet = mock(RestrictionSet.class);
        when(restrictionSet.isPermitted(any())).thenReturn(hasAccess);
        
        when(userDetails.getCollectionRestriction()).thenReturn(restrictionSet);
        
        RestrictionSet restrictionSetProfile = mock(RestrictionSet.class);
        when(restrictionSetProfile.isPermitted(any())).thenReturn(hasAccessToprofile);
        when(userDetails.getProfileRestriction()).thenReturn(restrictionSetProfile);
        
        when(user.getUserInfoDetails()).thenReturn(userDetails);
        
        when(currentFunnelbackUserHelper.getCurrentFunnelbackUser()).thenReturn(user);
        
        return currentFunnelbackUserHelper;
        
    }
    
    private I18n mockI18n() {
        I18n i18n = mock(I18n.class);
        when(i18n.tr(anyString())).thenReturn("");
        return i18n;
    }
    
}
