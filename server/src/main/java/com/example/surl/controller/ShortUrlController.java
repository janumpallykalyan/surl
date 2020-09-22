package com.example.surl.controller;

import com.example.surl.controller.dto.*;
import com.example.surl.exception.*;
import com.example.surl.model.ShortUrl;
import com.example.surl.service.ShortUrlService;
import com.example.surl.util.Utility;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * REST controller for managing url shortening , expansion and statistics Operation.
 *
 * @author Kalyan J
 */
@RestController
public class ShortUrlController {

    private ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    /**
     * Get /api/v1/:key  taking already generated short Url, redirect user to the corresponding long url
     *
     * @param key      shortUrl provided by user
     * @param request  it is used to extract browser and os information of analytics
     * @param response it is used to redirect user to the appropriate long url
     * @throws KeyNotFoundException    if the provided shortUrl is not available in the DB
     * @throws InvalidAddressException if the key (or short url) is empty or null
     */
    @GetMapping("/api/v1/{key}")
    public void expandingUrl(@PathVariable String key, HttpServletRequest request, HttpServletResponse response) throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException {

        ResolveLinkDto dto = new ResolveLinkDto();
        dto.setBrowser(Utility.getBrowserType(request));
        dto.setOs(Utility.getOperatingSystemType(request));
        dto.setUserInfo(Utility.getUserInfo(request));
        dto.setShortUrl(key);

        String longUrl = shortUrlService.resolve(dto).getLongUrl();

        response.setHeader("Location", longUrl);
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }

    /**
     * POST /api/v1/shortify        longurl will be posted to this endpoint that corresponding short url be generated
     * and returned
     *
     * @param dto a container to hold the provided long url
     * @return Output the generated short url in the message item of the json.
     * @throws MalformedURLException if the format of the provided url is not valid
     * @throws UnknownHostException  if the system requesting, does not have proper hostname
     * @throws KeyOverFlowException  if the system has exhausted the maximum amount of counters
     */
    @PostMapping("/api/v1/shortify")
    public ResponseEntity<BaseResponse> assignNewKey(@RequestBody NewLinkDto dto) throws MalformedURLException, UnknownHostException, KeyOverFlowException, URISyntaxException {

        String key = shortUrlService.shorten(dto);
        return ResponseEntity.created(new URI("/api/v1/" +key)).body(new BaseResponse(true, key, BaseResponse.SUCCESSFUL));
    }

    /**
     * GET /api/v1/users/:id/surls   user's shortUrl list will be returned
     *
     * @param id short url that already generated by the service.
     * @return Output the calculated statistics gathered by the service.
     * @throws UserIdNotFoundException if the provided shortUrl is not available in the DB
     */
    //TODO Pagination not implemented
    @GetMapping("/api/v1/users/{id}/surls")
    public ResponseEntity<List<ShortUrlDto>> getShortUrlList(@PathVariable String id) throws UserIdNotFoundException {
        List<ShortUrlDto> dto = shortUrlService.getShortUrlList(id);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * GET /api/v1/stat/:key   stat will be calculated and returned
     *
     * @param key short url that already generated by the service.
     * @return Output the calculated statistics gathered by the service.
     * @throws KeyNotFoundException if the provided shortUrl is not available in the DB
     */
    @GetMapping("/api/v1/stat/{key}")
    public ResponseEntity<BaseResponse> getStats(@PathVariable String key) throws KeyNotFoundException {
        VisitStateDto dto = shortUrlService.getVisitStateByKey(key);
        return ResponseEntity.ok().body(dto);
    }

    /**
     * POST /api/v1/users/{id}        update user info
     * 
     *
     * @param dto a container to hold user info
     * @return Output the succuss if saved.
     * @throws Exception if the infomation is not saved or service exception

     */
    @PostMapping("/api/v1/users")
    public ResponseEntity<BaseResponse> updateUserInfo(@RequestBody UserDto dto) throws Exception {
        dto = shortUrlService.saveUserInfo(dto);
        return ResponseEntity.ok().body(dto);
    }


}
