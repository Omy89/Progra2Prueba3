package progra2prueba3;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;

public class EmpleadoManager {

    private RandomAccessFile rcods, remps;

    /*
        Formato Codigo.emp
        int code
        Formato Empleados.emp
        int code
        String name
        double salary
        long fecha Contratacion
        long fecha despido
     */
    public EmpleadoManager() {
        try {
            //1 - Asegurar que el folder company exista
            File mf = new File("company");
            mf.mkdir();
            //2 - Instanciar RAFs dentro de company
            rcods = new RandomAccessFile("company/codigos.emp", "rw");
            remps = new RandomAccessFile("company/empleado.emp", "rw");
            initCodes();
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());
        }
    }

    private void initCodes() throws IOException {
        if (rcods.length() == 0) {
            //Puntero ->   0
            rcods.writeInt(1);
            //Puntero ->   4
        }
    }

    private int getCode() throws IOException {
        rcods.seek(0);
        //Puntero     ->       0
        int code = rcods.readInt();
        //Puntero     ->       4
        rcods.seek(0);
        rcods.writeInt(code + 1);
        return code;
    }

    public void addEmployee(String name, double salary) throws IOException {
        //Asegurar que el puntero este en el final del archivo
        remps.seek(remps.length());
        int code = getCode();
        //P -> 0
        remps.writeInt(code);
        //P -> 4
        remps.writeUTF(name);//Ana 8
        //p -> 12
        remps.writeDouble(salary);
        //P -> 20
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        //P -> 28
        remps.writeLong(0);
        //P -> 36 EOF
        //Asegurar crear folder y archivos individuales
        createEmployeeFolders(code);
    }

    private String employeeFolder(int code) {
        return "company/empleado" + code;
    }

    private void createEmployeeFolders(int code) throws IOException {
        //Crear folder empleado+code
        File empDIR = new File(employeeFolder(code));
        empDIR.mkdir();
    }

    private RandomAccessFile salesFileFor(int code) throws IOException {
        String DIRpadre = employeeFolder(code);
        int yearActual = Calendar.getInstance().get(Calendar.YEAR);
        String path = DIRpadre + "/ventas" + yearActual + ".emp";
        return new RandomAccessFile(path, "rw");
    }

    private void createYearSalesFileFor(int code) throws IOException {

        RandomAccessFile ryear = salesFileFor(code);
        if (ryear.length() == 0) {
            for (int mes = 0; mes < 12; mes++) {
                ryear.writeDouble(0);
                ryear.writeBoolean(false);

            }

        }
    }

    //Code - Name - Salary - Fecha Con.
    public void employeeList() throws IOException {
        remps.seek(0);
        //P-->  36 < 36 False
        while (remps.getFilePointer() <= remps.length()) {
            //P->0
            int code = remps.readInt();
            //P->4
            String name = remps.readUTF();
            //P->12
            double salary = remps.readDouble();
            //P-> 20
            Date dateH = new Date(remps.readLong());
            //P->28
            if (remps.readLong() == 0) {
                System.out.println("Codigo: " + code + "Nombre: " + name
                        + "Salario: Lps." + salary + "Contratado: " + dateH);
            }
            //P->36

        }

    }

    private boolean isEmployeeActive(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int codigo = remps.readInt();
            long pos = remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if (remps.readLong() == 0 && codigo == code) {
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }

    public boolean fireEmployee(int code) throws IOException {
        if (isEmployeeActive(code)) {
            String name = remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: " + name);
            return true;
        }
        return false;
    }

    public void addSaleToEmployee(int code, double monto) throws IOException {
        if (isEmployeeActive(code)) {
            RandomAccessFile salesFile = salesFileFor(code);
            createYearSalesFileFor(code);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            salesFile.seek(currentMonth * 9);
            double currentSales = salesFile.readDouble();
            salesFile.seek(currentMonth * 9);
            salesFile.writeDouble(currentSales + monto);
            System.out.println("Venta de " + monto + " anadida al empleado con codigo " + code);
        } else {
            System.out.println("Empleado no encontrado o no activo");
        }
    }

    public void payEmployee(int code) throws IOException {
        if (!isEmployeeActive(code)) {
            System.out.println("Empleado no encontrado o no activo");
            return;
        }

        RandomAccessFile salesFile = salesFileFor(code);
        createYearSalesFileFor(code);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        salesFile.seek(currentMonth * 9 + 8);
        if (salesFile.readBoolean()) {
            System.out.println("El empleado ya recibio su pago eeste mes");
            return;
        }

        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int empCode = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            remps.readLong();
            remps.readLong();
            if (empCode == code) {
                salesFile.seek(currentMonth * 9);
                double monthlySales = salesFile.readDouble();
                double commission = monthlySales * 0.15;
                double baseSalary = salary + commission;
                double deduction = baseSalary * 0.01;
                double netSalary = baseSalary - deduction;

                String receiptPath = employeeFolder(code) + "/recibos.emp";
                try (RandomAccessFile receiptFile = new RandomAccessFile(receiptPath, "rw")) {
                    receiptFile.seek(receiptFile.length());
                    receiptFile.writeLong(System.currentTimeMillis());
                    receiptFile.writeDouble(commission);
                    receiptFile.writeDouble(baseSalary);
                    receiptFile.writeDouble(deduction);
                    receiptFile.writeDouble(netSalary);
                    receiptFile.writeInt(Calendar.getInstance().get(Calendar.YEAR));
                    receiptFile.writeInt(currentMonth + 1);
                }
                salesFile.seek(currentMonth * 9 + 8);
                salesFile.writeBoolean(true);
                System.out.println("Pago realizado a " + name);
                System.out.println("Sueldo neto: Lps. " + netSalary);
                return;
            }
        }
        System.out.println("Empleado no encontrado");
    }

    public void printEmployee(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int empCode = remps.readInt();
            String name = remps.readUTF();
            double salary = remps.readDouble();
            long hireDate = remps.readLong();
            remps.readLong();

            if (empCode == code) {
                System.out.println("Codigo " + empCode);
                System.out.println("Nombre: " + name);
                System.out.println("Salario: Lps. " + salary);
                System.out.println("Fecha de contratacion: " + new Date(hireDate));

                RandomAccessFile salesFile = salesFileFor(code);
                createYearSalesFileFor(code);
                double totalSales = 0;
                System.out.println("Ventas anuales:");
                for (int i = 0; i < 12; i++) {
                    salesFile.seek(i * 9);
                    double monthlySales = salesFile.readDouble();
                    totalSales += monthlySales;
                    System.out.println("Mes " + (i + 1) + ": Lps. " + monthlySales);
                }
                System.out.println("Total de ventas: Lps. " + totalSales);

                String receiptPath = employeeFolder(code) + "/recibos.emp";
                File receiptFile = new File(receiptPath);
                if (receiptFile.exists()) {
                    try (RandomAccessFile receipts = new RandomAccessFile(receiptFile, "r")) {
                        int receiptCount = 0;
                        while (receipts.getFilePointer() < receipts.length()) {
                            receipts.readLong();
                            receipts.readDouble();
                            receipts.readDouble();
                            receipts.readDouble();
                            receipts.readDouble();
                            receipts.readInt();
                            receipts.readInt();
                            receiptCount++;
                        }
                        System.out.println("Recibos: " + receiptCount);
                    }
                } else {
                    System.out.println("No se encontraron recibos viejos");
                }
                return;
            }
        }
        System.out.println("Empleado no encontrado");
    }
}
