package com.example.surl.service;

import com.example.surl.controller.dto.BaseResponse;
import com.example.surl.controller.dto.NewLinkDto;
import com.example.surl.controller.dto.ResolveLinkDto;
import com.example.surl.controller.dto.VisitStateDto;
import com.example.surl.exception.InvalidAddressException;
import com.example.surl.exception.KeyNotFoundException;
import com.example.surl.exception.KeyOverFlowException;
import com.example.surl.exception.ShortUrlExpiryException;
import com.example.surl.model.ShortUrl;
import com.example.surl.model.embedded.BrowserStats;
import com.example.surl.model.embedded.DateStat;
import com.example.surl.model.embedded.OsStat;
import com.example.surl.model.embedded.Stats;
import com.example.surl.repository.ShortUrlRepository;
import com.example.surl.service.WorkerStatusService;
import com.example.surl.service.impl.ShortUrlServiceImpl;
import com.example.surl.util.Base58;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShortUrlServiceTest {

    @InjectMocks
    private ShortUrlServiceImpl service;

    @Mock
    private WorkerStatusService workerStatusService;

    @Mock
    private ShortUrlRepository repository;


    @Before
    public void contextLoads() {
        MockitoAnnotations.initMocks(this);
    }

    private ShortUrl shortUtilInit() {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setLongUrl("http://www.google.com");
        shortUrl.setCreatedDate(LocalDate.now());
        Stats stats = new Stats();

        BrowserStats browserStats = new BrowserStats();
        browserStats.setChrome(19L);
        stats.setBrowserStats(browserStats);
        OsStat osStat = new OsStat();
        osStat.setIos(10L);
        stats.setOsStat(osStat);
        DateStat dateStat1 = new DateStat();
        dateStat1.setDayOfYear(29);
        dateStat1.setVisits(2);
        DateStat dateStat2 = new DateStat();
        dateStat2.setDayOfYear(33);
        dateStat2.setVisits(1);

        DateStat dateStat3 = new DateStat();
        dateStat3.setDayOfYear(129);
        dateStat3.setVisits(23);

        stats.getDateStats().addAll(Arrays.asList(dateStat1, dateStat2, dateStat3));
        shortUrl.setStats(stats);
        return shortUrl;
    }

    private NewLinkDto newLinkDtoInit() {
        NewLinkDto newLinkDto = new NewLinkDto();
        newLinkDto.setLongUrl("http://www.google.com");
        return newLinkDto;
    }

    private ResolveLinkDto resolveLinkDtoInit() {
        ResolveLinkDto dto = new ResolveLinkDto();
        dto.setBrowser("chrome");
        dto.setOs("windows");
        dto.setShortUrl("b");
        dto.setWorkerId(1);
        Long key = 1L;
        return dto;
    }

//    @Test
//    public void should_resolveShortenedUrl_when_alreadyExists() throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException {
//        // Given
//        ShortUrl shortUrl = this.shortUtilInit();
//        ResolveLinkDto dto = resolveLinkDtoInit();
//        Long key = 1L;
//        when(repository.findByKeyCode(key)).thenReturn(shortUrl);
//
//
//        Long previousChromStat = shortUrl.getStats().getBrowserStats().getChrome();
//        Long previousWinStat = shortUrl.getStats().getOsStat().getWindows();
//
//        // When
//        ShortUrl shortenedUrl = service.resolve(dto);
//
//        // Then
//        assertThat(shortenedUrl).isNotNull();
//        assertThat(shortenedUrl.getLongUrl()).isEqualTo("http://www.google.com");
//        assertThat(shortUrl.getStats().getBrowserStats().getChrome()).isEqualTo(previousChromStat + 1);
//        assertThat(shortUrl.getStats().getOsStat().getWindows()).isEqualTo(previousWinStat + 1);
//
//    }


    @Test(expected = KeyNotFoundException.class)
    public void should_throwException_when_shortUrlDoesNotExist() throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException {
        // Given
        ResolveLinkDto dto = resolveLinkDtoInit();

        // When
        ShortUrl shortenedUrl = service.resolve(dto);

    }

    @Test(expected = InvalidAddressException.class)
    public void should_throwException_when_shortUrlNotProvided() throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException {
        // Given
        ResolveLinkDto dto = resolveLinkDtoInit();
        dto.setShortUrl(null);

        // When
        ShortUrl shortenedUrl = service.resolve(dto);

    }


    @Test
    public void should_generateShortenedUrl_when_urlIsValidAndDoesNotExist() throws MalformedURLException, UnknownHostException, KeyOverFlowException {

        // Given
        NewLinkDto dto = newLinkDtoInit();
        long key = 1L;

        when(workerStatusService.getNewKey(anyString())).thenReturn(key);

        // When
        String shortedUrl = service.shorten(dto);

        // Then
        assertThat(shortedUrl).isNotNull();
        assertThat(shortedUrl).isEqualTo(Base58.fromBase10(key));
    }


    @Test(expected = MalformedURLException.class)
    public void should_throwException_when_urlIsNotValid() throws MalformedURLException, UnknownHostException, KeyOverFlowException {

        // Given
        NewLinkDto dto = newLinkDtoInit();
        dto.setLongUrl("badURL");

        // When
        String shortedUrl = service.shorten(dto);
    }


    @Test
    public void should_calculateStatistics_when_ShortUrlExist() throws KeyNotFoundException {
        // Given
        Long key = 1L;
        when(repository.findByKeyCode(key)).thenReturn(shortUtilInit());
        //When
        VisitStateDto dto = service.getVisitStateByKey(Base58.fromBase10(key));
        //Then
        assertThat(dto.getDailyAverage()).isNotNull();
        assertThat(dto.getMax()).isNotNull();
        assertThat(dto.getMin()).isNotNull();
        assertThat(dto.getTotalPerYear()).isNotNull();
        assertThat(dto.getPerMonth()).isNotNull();
        assertThat(dto.getByBrowsers()).isNotNull();
        assertThat(dto.getByOs()).isNotNull();
        assertThat(dto.getCode()).isEqualTo(BaseResponse.SUCCESSFUL);
        assertThat(dto.getMessage()).isEqualTo("analytics");
    }


    @Test(expected = KeyNotFoundException.class)
    public void should_throwExceptionOnStatCalculation_when_shortUrlDoesNotExist() throws KeyNotFoundException {
        // Given
        Long key = 1L;

        // When
        VisitStateDto dto = service.getVisitStateByKey(Base58.fromBase10(key));

    }


}
