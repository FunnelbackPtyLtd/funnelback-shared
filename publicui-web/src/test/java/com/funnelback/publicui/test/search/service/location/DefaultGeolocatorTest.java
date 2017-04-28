package com.funnelback.publicui.test.search.service.location;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.location.DefaultGeolocator;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.maxmind.geoip.Location;



/**
 * @author luke
 *
 *
 */
public class DefaultGeolocatorTest {
    
    private DefaultGeolocator geolocator;
    private MockConfigRepository configRepository;
    private final String searchHome = "src/test/resources/dummy-search_home"; 
    
    private final String testIP = "150.203.239.15";
    private final String testResultCountry = "Australia";

    @Before
    public void before() throws FileNotFoundException {
        this.geolocator = new DefaultGeolocator();
        this.configRepository = new MockConfigRepository();
        this.configRepository.setGlobalConfiguration(new GlobalOnlyConfig(new File(searchHome)));
    }


    /**
     * This will test with a working database, if this fails it is likely the rest of the tests will fail.
     * @throws FileNotFoundException
     */
    @Test
    public void geolocateNolookupServiceSet() throws FileNotFoundException{
        
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        SearchQuestion question = new SearchQuestion();
        question.setRequestId(testIP);
        
        Location location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
        
        question.setRequestId("127.0.0.1");
        location = this.geolocator.geolocate(question);
        Assert.assertNull(location.countryName);
        
    }
    
    @Test 
    public void changeDataBaseTestMissingFile() {
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        SearchQuestion question = new SearchQuestion();
        question.setRequestId(testIP);
        
        Location location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
        
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, "NonExistantFile.nothere");
        this.geolocator.setConfigRepository(this.configRepository);
        location = this.geolocator.geolocate(question);
        Assert.assertNull("Changed to a non existant file.", location.countryName);
        
        //Now change it back and ensure it works
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
    }
    
    @Test 
    public void changeDataBaseTest() {
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        SearchQuestion question = new SearchQuestion();
        question.setRequestId(testIP);
        
        Location location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
        
        //Change to a bad data base
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoIP.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        //This is horrible but until the new data base format is released this will do.
        try {
            location = this.geolocator.geolocate(question);
            Assert.fail("The data base failed to change");
        } catch (Exception e){
            
        }
        
        //Now change it back and ensure it works
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
        
        //Change it to the same data base and ensure that works
        this.configRepository.getGlobalConfiguration().setValue(Keys.GEOLOCATION_DATABASE_KEY, searchHome + "/share/GeoLiteCity.dat");
        this.geolocator.setConfigRepository(this.configRepository);
        location = this.geolocator.geolocate(question);
        Assert.assertNotNull(location.countryName);
        Assert.assertEquals(location.countryName, testResultCountry);
    }
    
    /**
     * FUN-5494 Keys.GEOLOCATION_DATABASE_KEY is not set
     */
    @Test
    public void noGeolocationDataBaseSetTest(){
        this.geolocator.setConfigRepository(this.configRepository);
        SearchQuestion question = new SearchQuestion();
        question.setRequestId(testIP);
        
        Location location = this.geolocator.geolocate(question);
        Assert.assertNull(location.countryName);
    }
}
