@echo off
echo === Drone Simulation Project - Build and Run ===

:: Create output directory
if not exist bin mkdir bin

:: Compile all Java sources
echo Compiling...
javac --release 17 ^
      -cp "lib\json-20240303.jar" ^
      -d bin ^
      src\dronesim\exception\ApiException.java ^
      src\dronesim\model\DroneType.java ^
      src\dronesim\model\Drone.java ^
      src\dronesim\model\DroneDynamics.java ^
      src\dronesim\model\DroneMetric.java ^
      src\dronesim\api\PaginatedResult.java ^
      src\dronesim\api\ApiClient.java ^
      src\dronesim\api\DroneSimApiClient.java ^
      src\dronesim\config\AppConfig.java ^
      src\dronesim\util\AppLogger.java ^
      src\dronesim\util\JsonParser.java ^
      src\dronesim\service\DataService.java ^
      src\dronesim\service\DroneTypeService.java ^
      src\dronesim\service\DroneService.java ^
      src\dronesim\service\DroneDynamicsService.java ^
      src\dronesim\service\MetricsService.java ^
      src\dronesim\controller\DashboardController.java ^
      src\dronesim\gui\DroneTableModel.java ^
      src\dronesim\gui\DynamicsTableModel.java ^
      src\dronesim\gui\SettingsDialog.java ^
      src\dronesim\gui\DashboardPanel.java ^
      src\dronesim\gui\CatalogPanel.java ^
      src\dronesim\gui\FlightDynamicsPanel.java ^
      src\dronesim\gui\MainFrame.java ^
      src\dronesim\app\DroneSimApp.java

if errorlevel 1 (
    echo Compilation FAILED.
    pause
    exit /b 1
)

echo Compilation successful. Starting application...
java -cp "bin;lib\json-20240303.jar" dronesim.app.DroneSimApp

pause
