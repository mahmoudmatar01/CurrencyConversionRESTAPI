package com.example.bm_project.services.impl;


import com.example.bm_project.client.BaseCurrencyCompareClient;
import com.example.bm_project.dto.request.CurrenciesCompareRequestDto;
import com.example.bm_project.dto.response.CurrencyCompareDto;
import com.example.bm_project.logger.LoggerSingleton;
import com.example.bm_project.models.CurrencyCompareResponse;
import com.example.bm_project.exception.NotFoundCurrencyCodeException;
import com.example.bm_project.helper.Helper;
import com.example.bm_project.mapper.IMapper;
import com.example.bm_project.mapper.Mapper;
import com.example.bm_project.services.CurrenciesCompareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.bm_project.constant.StringConstants.CurrencyNotFountExceptionMessage;
import static com.example.bm_project.constant.StringConstants.DataReceivedFromApiSuccessfully;


@CacheConfig(cacheNames = "currenciesCompareCache")
@Service
public class CurrenciesCompareServiceImpl implements CurrenciesCompareService {

    LoggerSingleton logger = LoggerSingleton.getInstance();
    private  final BaseCurrencyCompareClient currencyCompareClient;
    private final IMapper mapper;
    private Helper helper;


    @Autowired
    public CurrenciesCompareServiceImpl(BaseCurrencyCompareClient currencyCompareClient, Mapper mapper) {
        this.currencyCompareClient = currencyCompareClient;
        this.mapper=mapper;
        this.helper = helper.getInstance();
        logger.logInfo(this.getClass(),"Client"+ DataReceivedFromApiSuccessfully +"CurrencyCompare API");;
    }

    @Cacheable(value = "currenciesCompareCaching")
    @Override
    public CurrencyCompareDto getCurrenciesCompareRate(CurrenciesCompareRequestDto currenciesCompareRequestDto) {


        /* Check if base currency and target currencies are valid or not
           if it is not valid the project will throw not found currency exception and
           if amount is positive and numeric or not if an amount is not valid
                 the project will throw an invalid amount exception
         */
        if(!helper.currencyIsExist(currenciesCompareRequestDto.getFirstTargetCode().toUpperCase())||!helper.currencyIsExist(currenciesCompareRequestDto.getSecondTargetCode().toUpperCase())){
            logger.logError(this.getClass(),CurrencyNotFountExceptionMessage);
            throw new NotFoundCurrencyCodeException(CurrencyNotFountExceptionMessage);
        }
        helper.throwException(currenciesCompareRequestDto.getBaseCode(),String.valueOf(currenciesCompareRequestDto.getAmount()));

        //save target currencies in list to filter response to only currencies that in this list
        List<String> targetList=List.of(
                currenciesCompareRequestDto.getFirstTargetCode(),
                currenciesCompareRequestDto.getSecondTargetCode()
        );

        // if everything is valid will return response model with response data
        CurrencyCompareDto response=mapper
                .convertCurrencyExchangeResponseToCurrencyCompareResponseDto(
                        currencyCompareClient
                                .getBaseCurrencyExchangeRate(
                                        currenciesCompareRequestDto
                                                .getBaseCode()
                                                .toUpperCase()
                                ),
                        currenciesCompareRequestDto
                                .getAmount());
        logger.logInfo(this.getClass(),"CurrencyCompare Data Model returned Successfully");


        // map response data to filter it to have only target currencies
        Map<String, Double> filteredConversionRates = new HashMap<>();
        for (String currency : targetList) {
            if (response.getConversionRates().containsKey(currency)) {
                filteredConversionRates.put(currency, response.getConversionRates().get(currency));
            }
        }
        logger.logInfo(this.getClass(),"CurrencyCompare Data Model filtered Successfully");

        // update conversion rate object in my response to new filtered object
        response.setConversionRates(filteredConversionRates);
        logger.logInfo(this.getClass(),"Conversion Rates is up-to-date now");

        Map<String, Double> map = new HashMap<>();
        logger.logInfo(this.getClass(),"CurrencyCompare Response returned Successfully");
        map.put("firstTargetRate",response.getConversionRates().get(currenciesCompareRequestDto.getFirstTargetCode()));
        map.put("secondTargetRate",response.getConversionRates().get(currenciesCompareRequestDto.getSecondTargetCode()));
        response.setConversionRates(map);
        return response;

    }
}
