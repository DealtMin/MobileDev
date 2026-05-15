package ru.mirea.belkinaed.yandexdriver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;

import ru.mirea.belkinaed.yandexdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener{
    private Point routeStartLocation = new Point(55.670005, 37.479894);
    private final Point ROUTE_END_LOCATION = new Point(55.690850, 37.564756);
    private final Point screenCenter = new Point(
            (routeStartLocation.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
            (routeStartLocation.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2);
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isRouteRequested = false;
    private static final int REQUEST_CODE_PERMISSION = 200;
    private final int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF00BFFF, 0xFF0000FF};

    private final InputListener inputListener = new InputListener() {
        @Override
        public void onMapTap(@NonNull Map map, @NonNull Point point) {
            touchScreen();
            Log.d("MIREA", "---Map Tap---");
        }

        @Override
        public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        int internetPermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int coarsePermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int finePermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (internetPermissionStatus != PackageManager.PERMISSION_GRANTED ||
                coarsePermissionStatus != PackageManager.PERMISSION_GRANTED ||
                finePermissionStatus != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_PERMISSION);
        }

        mapView = binding.mapview;
        mapView.getMapWindow().getMap().setRotateGesturesEnabled(false);
        // Устанавливаем начальную точку и масштаб
        mapView.getMapWindow().getMap().move(new CameraPosition(screenCenter, 10, 0, 0));

        // Добавляем слушатель нажатий на карту
        mapView.getMapWindow().getMap().addInputListener(inputListener);

        // Инициализируем объект для создания маршрута водителя
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMapWindow().getMap().getMapObjects().addCollection();

        locationManager = MapKitFactory.getInstance().createLocationManager();
        locationListener = new LocationListener() {
            @Override
            public void onLocationUpdated(@NonNull Location location) {
                if (!isRouteRequested) {
                    routeStartLocation = location.getPosition();
                    mapView.getMapWindow().getMap().move(new CameraPosition(routeStartLocation, 10, 0, 0));
                    submitRequest();
                    isRouteRequested = true;
                }
            }

            @Override
            public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {
            }
        };
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        locationManager.unsubscribe(locationListener);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        subscribeToLocationUpdates();
    }

    private void subscribeToLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.subscribeForLocationUpdates(0.0, 1000, 1.0, false, FilteringMode.OFF, locationListener);
        }
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        // Кол-во альтернативных путей
        drivingOptions.setRoutesCount(4);
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        // Установка точек маршрута
        requestPoints.add(new RequestPoint(routeStartLocation, RequestPointType.WAYPOINT, null, null));
        requestPoints.add(new RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null, null));
        // Отправка запроса на сервер
        drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        for (int i = 0; i < list.size(); i++) {
            // настраиваем цвета для каждого маршрута
            int color = colors[i];
            // добавляем маршрут на карту
            mapObjects.addPolyline(list.get(i).getGeometry()).setStrokeColor(color);
        }
    }

    private void touchScreen() {
        PlacemarkMapObject marker = mapView.getMapWindow().getMap().getMapObjects().addPlacemark();
        marker.setGeometry(new Point(55.690850, 37.564756));
        marker.setIcon(ImageProvider.fromResource(this, android.R.drawable.arrow_down_float));
        marker.addTapListener((mapObject, point) -> {
            Toast.makeText(getApplication(), "Marker click",
                    Toast.LENGTH_SHORT).show();
            return false;
        });
    }
}