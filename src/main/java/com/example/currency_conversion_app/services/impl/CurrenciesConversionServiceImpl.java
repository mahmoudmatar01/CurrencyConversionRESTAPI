package com.example.currency_conversion_app.services.impl;


import com.example.currency_conversion_app.client.BaseCurrenciesConversionClient;
import com.example.currency_conversion_app.dto.response.CurrenciesConversionDto;
import com.example.currency_conversion_app.logger.Logger;
import com.example.currency_conversion_app.exception.NotFoundCurrencyCodeException;
import com.example.currency_conversion_app.helper.Helper;
import com.example.currency_conversion_app.mapper.IMapper;
import com.example.currency_conversion_app.mapper.Mapper;
import com.example.currency_conversion_app.services.CurrenciesConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.example.currency_conversion_app.constant.StringConstants.*;


@CacheConfig(cacheNames = "CurrenciesConversionCaching")
@Service
public class CurrenciesConversionServiceImpl implements CurrenciesConversionService {

    private Logger logger ;
    private  final BaseCurrenciesConversionClient currenciesConversionClient;
    private final IMapper mapper;
    private Helper helper;

    @Autowired
    public CurrenciesConversionServiceImpl(BaseCurrenciesConversionClient currenciesConversionRepo, Mapper mapper) {
        this.currenciesConversionClient = currenciesConversionRepo;
        this.mapper=mapper;
        this.helper = helper.getInstance();
        this.logger=logger.getInstance();
        logger.logInfo(this.getClass(),"Client"+ DataReceivedFromApiSuccessfully +"CurrencyConversion API");
    }

    @Cacheable(value = "CurrenciesConversionCache")
    @Override
    public CurrenciesConversionDto getCurrenciesConversionRate(String baseCurrency, String targetCurrency, String amount) {
                /* Check if base currency and target currencies are valid or not
        if it is not valid the project will throw not found currency exception  */
         /* Check if base currency and target currencies are valid or not
           if it is not valid the project will throw not found currency exception and
           if amount is positive and numeric or not if an amount is not valid
                 the project will throw an invalid amount exception
         */
        helper.throwException(baseCurrency,amount);
        if(!helper.currencyIsExist(targetCurrency.toUpperCase())){
            logger.logError(this.getClass(),CurrencyNotFountExceptionMessage);
            throw new NotFoundCurrencyCodeException(CurrencyNotFountExceptionMessage);
        }
        // receives data from data client and map it and then return response data
        logger.logInfo(this.getClass(),DataConvertedToDTO);
        return mapper.convertCurrenciesConversionResponseToCurrenciesConversionDto(
                currenciesConversionClient.currenciesConversion(baseCurrency.toUpperCase(),
                        targetCurrency.toUpperCase(),
                        amount)
        );
    }
}