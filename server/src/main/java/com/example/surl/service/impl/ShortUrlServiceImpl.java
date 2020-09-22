package com.example.surl.service.impl;

import com.example.surl.controller.dto.*;
import com.example.surl.exception.*;
import com.example.surl.model.ShortUrl;
import com.example.surl.model.ShortUrlLog;
import com.example.surl.model.User;
import com.example.surl.model.embedded.BrowserStats;
import com.example.surl.model.embedded.DateStat;
import com.example.surl.model.embedded.OsStat;
import com.example.surl.model.embedded.Stats;
import com.example.surl.repository.ShortUrlLogRepository;
import com.example.surl.repository.ShortUrlRepository;
import com.example.surl.repository.UserRepository;
import com.example.surl.service.EmailService;
import com.example.surl.service.ShortUrlService;
import com.example.surl.service.WorkerStatusService;
import com.example.surl.util.Base58;
import com.example.surl.util.Utility;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Service for shortening , expanding and providing statistics
 *
 * @author Kalyan J
 */
@Service
public class ShortUrlServiceImpl implements ShortUrlService {
    private ShortUrlRepository shortUrlRepository;
    private ShortUrlLogRepository shortUrlLogRepository;
    private UserRepository userRepository;
    private WorkerStatusService workerStatusService;
    private EmailService emailService;


    public ShortUrlServiceImpl(ShortUrlRepository shortUrlRepository, ShortUrlLogRepository shortUrlLogRepository, UserRepository userRepository, WorkerStatusService workerStatusService, EmailService emailService, Environment env) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlLogRepository = shortUrlLogRepository;
        this.userRepository = userRepository;
        this.workerStatusService = workerStatusService;
        this.emailService = emailService;
    }

    /**
     * the service that provides shortening url
     * for shortening purpose, workerID (docker Container instance hostname) should be extracted
     * and based on that a new code is used to be encoded in base58.
     * after code assignment , created date , stats are also initialized.
     *
     * @param linkDto a container to hold the provided long url
     * @return short url generated based on base58 encoding mechanism
     * @throws MalformedURLException if the format of the provided url is not valid
     * @throws UnknownHostException  if the system requesting, does not have proper hostname
     * @throws KeyOverFlowException  if the system has exhausted the maximum amount of counters
     */
    @Override
    public String shorten(NewLinkDto linkDto) throws UnknownHostException, MalformedURLException, KeyOverFlowException {
        String workerID = Utility.getHostname();
        linkDto.setLongUrl(Utility.urlNormalization(linkDto.getLongUrl()));

        if (!Utility.isUrlValid(linkDto.getLongUrl())) throw new MalformedURLException();
        Optional<ShortUrl> existingShortUrl = Optional.ofNullable(shortUrlRepository.findByLongUrlAndUserId(linkDto.getLongUrl(), linkDto.getUserId()));
        if (existingShortUrl.isPresent()) return Base58.fromBase10(existingShortUrl.get().getKeyCode());

        Long newKey = workerStatusService.getNewKey(workerID);
        ShortUrl newShortUrl = new ShortUrl();
        newShortUrl.setKeyCode(newKey);
        newShortUrl.setUserId(linkDto.getUserId());
        newShortUrl.setLongUrl(linkDto.getLongUrl());
        newShortUrl.setStats(this.initState());
        newShortUrl.setCreatedDate(LocalDate.now());
        newShortUrl.setExpiryDate(LocalDate.now().plusDays(3));

        shortUrlRepository.save(newShortUrl);
        return Base58.fromBase10(newKey);

    }

    /**
     * once shortUrl is provided , this method will return corresponding url by decoding and converting in back
     * to decimal system and finding appropriate long url with that decimal key in the DB
     *
     * @param dto container that holds requesting browser , os and shortUrl
     * @return founded ShortUrl Entity matching the given in the dto shortUrl.
     * @throws KeyNotFoundException    if the provided shortUrl is not available in the DB
     * @throws InvalidAddressException if the key (or short url) is empty or null
     */
    @Override
    public ShortUrl resolve(ResolveLinkDto dto) throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException {
        if (dto.getShortUrl() == null || "".equals(dto.getShortUrl())) throw new InvalidAddressException();
        ShortUrl shortUrl = Optional.ofNullable(shortUrlRepository.findByKeyCode(Base58.toBase10(dto.getShortUrl())))
                .map(c -> c)
                .orElseThrow(KeyNotFoundException::new);
        if(shortUrl.getExpiryDate().isBefore(LocalDate.now())) {
            if(shortUrl.isNotifiedExpiry()) {
                User user = userRepository.findByUserId(shortUrl.getUserId());
                String subject = "Short URL is Expired, Please Regenerate";
                String message = "Hello " + user.getName() +",\n" + "Your Short URL " +  "http://domain/" + Base58.fromBase10(shortUrl.getKeyCode()) + "expired. \n"
                        + "For Actual URL, " + shortUrl.getLongUrl() + " You can regenerate Short URL.";
                emailService.sendSimpleMessage(user.getEmail(), subject, message);
            }
            throw new ShortUrlExpiryException();
        }
        ShortUrlLog surlLog = new ShortUrlLog();
        surlLog.setKeyCode(Base58.toBase10(dto.getShortUrl()));
        this.updateStats(dto, shortUrl, surlLog);

        shortUrl.setLastAccessDate(LocalDate.now());
        shortUrlLogRepository.save(surlLog);
        shortUrlRepository.save(shortUrl);
        return shortUrl;
    }

    /**
     * give the list of shortUrl from specific user will be returned
     *
     * @param userId give the userId generated at client side,
     * @return the list of shortUrl
     * @throws UserIdNotFoundException if the provided userId is not available in the DB
     */
    @Override
    public List<ShortUrlDto> getShortUrlList(String userId) throws UserIdNotFoundException {
        List<ShortUrl> shortUrlList =  Optional.ofNullable(shortUrlRepository.findByUserId(userId))
                .map(c -> c)
                .orElseThrow(UserIdNotFoundException::new);
        List<ShortUrlDto> shortUrlDtoList = new ArrayList<>();
        shortUrlList.forEach(shortUrl -> {
            ShortUrlDto shortUrlDto = new ShortUrlDto();
            shortUrlDto.setKeyCode(Base58.fromBase10(shortUrl.getKeyCode()));
            shortUrlDto.setLastAccessDate(shortUrl.getLastAccessDate());
            shortUrlDto.setLongUrl(shortUrl.getLongUrl());
            shortUrlDto.setUserId(shortUrl.getUserId());
            LongSummaryStatistics longSummaryStatistics = shortUrl.getStats().getDateStats().stream().mapToLong(d -> d.getVisits()).summaryStatistics();
            shortUrlDto.setTotalVisits(longSummaryStatistics.getSum());
            shortUrlDtoList.add(shortUrlDto);
        });
        return shortUrlDtoList;
    }

    @Override
    public UserDto saveUserInfo(UserDto dto) throws Exception {
        User user = new User();
        user.setEmail(dto.getEmailId());
        user.setName(dto.getName());
        user.setUserId(dto.getUserId());
        userRepository.save(user);
        return dto;
    }

    /**
     * give the shortUrl generated code, statistics is calculated and will be returned
     *
     * @param key give the shortUrl generated code,
     * @return analytics information for the give shortUrl generated code in the VisitStateDto Obj
     * @throws KeyNotFoundException if the provided shortUrl is not available in the DB
     */
    public VisitStateDto getVisitStateByKey(String key) throws KeyNotFoundException {
        VisitStateDto dto = new VisitStateDto();
        ShortUrl shortUrl = Optional.ofNullable(shortUrlRepository.findByKeyCode(Base58.toBase10(key)))
                .map(c -> c)
                .orElseThrow(KeyNotFoundException::new);


        LongSummaryStatistics longSummaryStatistics = shortUrl.getStats().getDateStats().stream().mapToLong(d -> d.getVisits()).summaryStatistics();
        dto.setDailyAverage(longSummaryStatistics.getAverage());
        dto.setMax(longSummaryStatistics.getMax());
        dto.setMin(longSummaryStatistics.getMin());
        dto.setTotalPerYear(longSummaryStatistics.getSum());
        dto.setPerMonth(getMonthlyVisitReport(shortUrl));
        dto.setByOs(shortUrl.getStats().getOsStat());
        dto.setByBrowsers(shortUrl.getStats().getBrowserStats());
        dto.setLastAccessDate(shortUrl.getLastAccessDate());
        dto.setCode(BaseResponse.SUCCESSFUL);
        dto.setSuccess(true);
        dto.setMessage("analytics");

        return dto;
    }

    /**
     * once expanding request, the stats such as browser and os requesting , nth day of year request has come are updated
     *
     * @param dto      container that holds requesting browser , os and shortUrl
     * @param shortUrl founded shortUrl from DB , to be updated
     * @return stats updated obj of the requested shortURL
     */
    private ShortUrl updateStats(ResolveLinkDto dto, ShortUrl shortUrl, ShortUrlLog shortUrlLog) {
        shortUrlLog.setAccessedByBrowser(dto.getBrowser().toLowerCase());
        switch (dto.getBrowser().toLowerCase()) {
            case "internet explorer":
                shortUrl.getStats().getBrowserStats().incrementIe();
                break;
            case "safari":
                shortUrl.getStats().getBrowserStats().incrementSafari();
                break;
            case "chrome":
                shortUrl.getStats().getBrowserStats().incrementChrome();
                break;
            case "firefox":
                shortUrl.getStats().getBrowserStats().incrementFireFox();
                break;
            case "opera":
                shortUrl.getStats().getBrowserStats().incrementOpera();
                break;

            default:
                shortUrl.getStats().getBrowserStats().incrementOthers();
                break;
        }
        shortUrlLog.setAccessedByDevice(dto.getOs().toLowerCase());
        switch (dto.getOs().toLowerCase()) {
            case "windows":
                shortUrl.getStats().getOsStat().incrementWindows();
                break;
            case "mac_os":
                shortUrl.getStats().getOsStat().incrementMacOs();
                break;
            case "linux":
                shortUrl.getStats().getOsStat().incrementLinux();
                break;
            case "android":
                shortUrl.getStats().getOsStat().incrementAndroid();
                break;
            case "iphone":
                shortUrl.getStats().getOsStat().incrementIos();
                break;

            default:
                shortUrl.getStats().getOsStat().incrementOthers();
                break;
        }
        shortUrlLog.setAccessedDate(LocalDate.now());
        shortUrlLog.setAccessedByUser(dto.getUserInfo());
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);


        shortUrl.getStats().getDateStats().stream().filter((d) -> d.getDayOfYear() == dayOfYear).findFirst().map(d -> {
            d.incrementVisit();
            return d;
        }).orElseGet(() -> {
            DateStat newDateStat = new DateStat(dayOfYear, 1);
            shortUrl.getStats().getDateStats().add(newDateStat);
            return newDateStat;
        });

        return shortUrl;
    }

    /**
     * stats initialized once new shortening request comes to service
     *
     * @return initialized Stats
     */
    private Stats initState() {
        Stats state = new Stats();
        state.setBrowserStats(new BrowserStats());
        state.setOsStat(new OsStat());
        return state;
    }

    /**
     * request are recorded as the nth day of the year in the DateStats,so this methos loops through the each month
     * and calculates total visit of each month by summing during that month.
     *
     * @param shortUrl founded shortUrl from DB for which date are going to be calculated monthly
     * @return a map of that key represent month name, and value is equal to the sum of visits in that month
     */
    private Map<String, Long> getMonthlyVisitReport(ShortUrl shortUrl) {
        int year = LocalDate.now().getYear();
        Map<String, Long> monthlyVisitsReport = new HashMap<>();
        for (LocalDate date = LocalDate.of(year, 1, 1); date.isBefore(LocalDate.of(year + 1, 1, 1)); date = date.plusMonths(1)) {
            String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
            Long totalVisits = this.getTotalVisitForAMonth(shortUrl, date.getDayOfYear(), date.plusMonths(1).getDayOfYear());
            monthlyVisitsReport.put(month, totalVisits);
        }

        return monthlyVisitsReport;
    }

    /**
     * a helper to calculate the sum of visit during a date range by startDay number of year to endDay number in the year
     *
     * @param shortUrl founded shortUrl from DB for which date are going to be calculated monthly
     * @param startDay start nth day of the range
     * @param lastDay  end nth day of the range
     * @return sum of visits in the duration
     */
    private Long getTotalVisitForAMonth(ShortUrl shortUrl, Integer startDay, Integer lastDay) {
        return shortUrl.getStats().getDateStats().stream().filter(d -> d.getDayOfYear() >= startDay && d.getDayOfYear() < lastDay).mapToLong(d -> d.getVisits()).sum();
    }

}
