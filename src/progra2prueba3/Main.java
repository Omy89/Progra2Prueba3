package progra2prueba3;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner lea = new Scanner(System.in);
        EmpleadoManager manager = new EmpleadoManager();
        int ele;
        
        while (true) {
            System.out.println(" 1- Agregar Empleado              ");
            System.out.println(" 2- Listar Empleados no Despedidos ");
            System.out.println(" 3- Agregar Venta al Empleado      ");
            System.out.println(" 4- Pagar Empleado                 ");
            System.out.println(" 5- Despedir Empleado              ");
            System.out.println(" 6- Imprimir Información del Empleado");
            System.out.println(" 7- Salir                          ");
            ele = lea.nextInt();

            try {
                if (ele == 1) {
                    System.out.print("Ingrese el nombre del empleado: ");
                    String name = lea.next();
                    System.out.print("Ingrese el salario del empleado: ");
                    double salary = lea.nextDouble();
                    manager.addEmployee(name, salary);
                } else if (ele == 2) {
                    manager.employeeList();
                } else if (ele == 3) {
                    System.out.print("Ingrese el código del empleado: ");
                    int code = lea.nextInt();
                    System.out.print("Ingrese el monto de la venta: ");
                    double monto = lea.nextDouble();
                    manager.addSaleToEmployee(code, monto);
                } else if (ele == 4) {
                    System.out.print("Ingrese el código del empleado: ");
                    int code = lea.nextInt();
                    manager.payEmployee(code);
                } else if (ele == 5) {
                    System.out.print("Ingrese el código del empleado: ");
                    int code = lea.nextInt();
                    manager.fireEmployee(code);
                } else if (ele == 6) {
                    System.out.print("Ingrese el código del empleado: ");
                    int code = lea.nextInt();
                    manager.printEmployee(code);
                } else if (ele == 7) {
                    break;
                } else {
                    System.out.println("Opción no válida");
                }
            } catch (IOException e) {
                System.out.println("Ha habido un error inespedo");
            }
        }
    }
}
