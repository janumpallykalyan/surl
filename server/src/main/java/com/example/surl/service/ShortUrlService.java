package com.example.surl.service;

import com.example.surl.controller.dto.*;
import com.example.surl.exception.*;
import com.example.surl.model.ShortUrl;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.List;

public interface ShortUrlService {

    String shorten(NewLinkDto linkDto) throws MalformedURLException, UnknownHostException, KeyOverFlowException;

    ShortUrl resolve(ResolveLinkDto dto) throws KeyNotFoundException, InvalidAddressException, ShortUrlExpiryException;

    VisitStateDto getVisitStateByKey(String key) throws KeyNotFoundException;

    List<ShortUrlDto> getShortUrlList(String userId) throws UserIdNotFoundException;

    UserDto saveUserInfo(UserDto dto) throws Exception;

}
