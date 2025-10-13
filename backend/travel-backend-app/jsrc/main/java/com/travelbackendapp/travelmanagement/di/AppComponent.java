package com.travelbackendapp.travelmanagement.di;

import com.travelbackendapp.BookingEventHandler;
import com.travelbackendapp.ReportsSender;
import com.travelbackendapp.TravelApiHandler;
import com.travelbackendapp.ReportsSender;
import com.travelbackendapp.travelmanagement.cron.BookingStatusCronHandler;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
        InfraModule.class,
        AwsModule.class,
        ServiceModule.class
})
public interface AppComponent {
    void inject(TravelApiHandler handler);
    void inject(BookingEventHandler handler);
    void inject(BookingStatusCronHandler handler);
    void inject(ReportsSender handler);
}
