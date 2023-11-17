package com.foodtrace.utils.sms;

import java.util.Map;

public interface MSGService {

    boolean send(Map map, String phone);
}
