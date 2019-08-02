package com.marketplace.web;

import org.springframework.hateoas.MediaTypes;

/**
 * Defines media types for Product API
 * 
 * <pre>
 * V1_JSON_UTF8 : "application/vnd.marketplace.v1+json;charset=UTF-8"
 * V1_HAL_UTF8 : "application/vnd.marketplace.v1+hal+json;charset=UTF-8"
 * 
 * Current API Version is v1 : vnd.marketplace.v1
 * </pre>
 * 
 * @author Nikhil Vasaikar
 *
 */
public class MarketPlaceMediaTypes extends MediaTypes {

  public static final String BASE_TYPE = "application";

  public static final String VND_TYPE = "vnd.marketplace";

  public static final String UTF_8 = "charset=UTF-8";

  public static final String JSON_TYPE = "json";

  public static final String HAL_TYPE = "hal" + "+" + JSON_TYPE;

  public static final String VERSION_V1 = "v1";

  public static final String V1_HAL_UTF8 =
      BASE_TYPE + "/" + VND_TYPE + "." + VERSION_V1 + "+" + HAL_TYPE + ";" + UTF_8;

  public static final String V1_JSON_UTF8 =
      BASE_TYPE + "/" + VND_TYPE + "." + VERSION_V1 + "+" + JSON_TYPE + ";" + UTF_8;

}
