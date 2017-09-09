package Splitter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class Splitter {

    //Наличие флага -d, который говорит о том, что для нумерации выходных файлов используются цифры
    boolean flagD = false;

    //Наличие флага -o со значением "-", который говорит о том, что имя входного файла используется для выходного
    boolean useFilename = false;

    //Тип разделения содержимого
    Type splitType;

    //Кол-во элементов в выходном файле по-умолчанию (100 по условию)
    int amount = 100;

    //Имя входного файла
    String filename;

    //Итоговое имя выходного файла по умолчанию (по условию)
    String splittedFilename = "x";

    //Содержимое файла
    List<String> content;

    //Конструктор
    public Splitter(String[] args) throws IOException {
        //Инициализация стандартных данных
        initialize(args);
        //Если не задан тип разделения, используется построчный (по условию)
        if (splitType == null) {
            splitType = Type.LINE;
        }
        //Подгрузка файла
        File file = new File(filename);
        //Если этого файла нет, кидается ошибка
        if (!file.exists()) throw new FileNotFoundException("File " + filename + " not found");
        //Подгрузка содержимого файла
        content = Files.readAllLines(file.toPath(), Charset.forName("Cp1251"));
    }

    //Разделение содержимого построчно
    public List<String> splitLine(){
        //Если метод вызван, но тип разделения не соответствует - кидается ошибка
        if (splitType != Type.LINE) throw new IllegalStateException();
        //Сборка результирующих (разбитых) строк
        List<String> result = new ArrayList<>();
        //Цикл, в котором автоматически считается количество элементов, на которые разбивается исходный текст файла
        //Используется целочисленное деление, т.к. в последней части файла может остаться меньше строк, чем задано
        int startSize = content.size();
        for (int i = 0; i < startSize / amount; i++) {
            //Добавление к результату соответсвующего числа строк и их соединие переносом строки
            result.add(String.join("\r\n", content.subList(0, amount)));
            //Удаление текущего элемента из сожержимого
            for (int j = 0; j < amount; j++)
                content.remove(0);

        }
        //Добавление последних строк, если их число не равно amount
        if (content.size() % amount != 0) result.add(String.join("\r\n", content));
        //Возврат собранных строк
        return result;
    }

    //Разделение содержимого посимвольно
    public List<String> splitChar() {
        //Если метод вызван, но тип не соответствует - кидается ошибка
        if (splitType != Type.CHAR) throw new IllegalStateException();
        //Сборка результирующих (разбитых) строк
        List<String> result = new ArrayList<>();
        //Соединение исходного содержимого через перенос строки
        String content = String.join("", this.content);
        //Проход по всему тексту
        int i = 0;
        while (i < content.length()) {
            //В рез-тат добавляется последовательность из нужной длины символов, учитывая последний сегмент,
            //в котором их кол-во может не соотвествовать нужной длине
            result.add(content.substring(i, Math.min(i + amount, content.length())));
            i += amount;
        }
        return result;
    }

    public byte[][] splitFile() throws IOException {
        //Если метод вызван, но тип не соответствует - кидается ошибка
        if (splitType != Type.FILE) throw new IllegalStateException();
        //Подгрузка файла
        File file = new File(filename);
        //Длина одного массива байт
        int arrayLength = (int)Math.ceil((double) file.length() / amount);
        //Создание двумерного массива результирующих байтов длины, равной кол-ву разбиений, с учетом последнего
        byte[][] result = new byte[amount][];
        //Побайтовое тчение файла
        byte[] bytes = Files.readAllBytes(file.toPath());
        //Проход по целочисленной части разбиений
        int length = (int)file.length() / arrayLength;
        for (int i = 0; i < length; i++) {
            //Сохранение "под-массив" байт нужной длины и с нужным сдвигом
            result[i] = Arrays.copyOfRange(bytes, i * arrayLength, (i+1) * arrayLength);
        }
        //Нахождение кол-ва последних байт, не вошедших в результат
        int del = (int)file.length() % arrayLength;
        //Если такие байты остались, то они сохраняются как последний элемент массива
        if (del != 0) result[result.length - 1] = Arrays.copyOfRange(bytes,bytes.length - del, bytes.length);
        return result;
    }

    public void save() throws IOException {
        //Выбор имени для файлов с разбитым содержимым с учетом флага -о со значением "-"
        splittedFilename = (useFilename) ? filename : splittedFilename;
        //Для построчного и посимвольного разбиаения используется одинаковый алгоритм, т.к. их результат - строки
        if (splitType == Type.LINE || splitType == Type.CHAR) {
            //Получение результирующих данных с учетом типа разбиения
            List<String> files = (splitType == Type.LINE) ? splitLine() : splitChar();
            //Сохранение файлов для полученного числа
            for (int i = 0; i < files.size(); i++) {
                //Генерирация имени выходного файла с учетов постфикса
                File file = new File(splittedFilename + getPostfix(i));
                //Запись содержимого через "врайтер(?)"
                FileWriter wr = new FileWriter(file);
                wr.write(files.get(i));
                wr.close();
            }
        } else {
            //Для байт то же самое, только с использованием другого "врайтера" и типа записываемых данных
            byte[][] files = splitFile();
            for (int i = 0; i < files.length; i++) {
                File file = new File(splittedFilename + getPostfix(i));
                FileOutputStream wr = new FileOutputStream(file);
                wr.write(files[i]);
                wr.close();
            }
        }
    }

    private String getPostfix(int i) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        //Изначальный постфикс, равный числовому значению
        String postfix = "" + i;
        //Если флаг -d не стоит, используется буквенная запись, в результате получается запись в обратном порядке
        if (!flagD) {
            postfix = "";
            int sub = i;
            //Добавление букв с помощью остатка от деления (записть числа в 26-ричной буквенной системе счисления)
            do {
                postfix += chars.charAt(sub % 26);
                sub /= 26;
            } while (sub > 0);
            //Если символ всего один, добавляется "a" (по условию)
            if (postfix.length() == 1) postfix += "a";
        }
        //Инвертирование строки из-за порядка, при наличии флага -d. Если его нет — возвращается численное значение
        return (!flagD) ? new StringBuilder(postfix).reverse().toString() : postfix;
    }

    //Инициализация флагов
    private void initialize(String[] args) {
        //Запись аргументов в виде итератора
        Iterator<String> arguments = Arrays.asList(args).iterator();
        //Проход по всем значениям
        while (arguments.hasNext()) {
            //Получение текущий элемент
            String arg = arguments.next();
            switch (arg) {
                //Обработка флага -d
                case "-d":
                    flagD = true;
                    break;
                case "-l":
                    //Если тип разделениею не был указан, он записывается, если был - кидается ошибка (по условию)
                    if (splitType == null) {
                        splitType = Type.LINE;
                        //После типа разделения идет число элементов, оно сразу сохраняется
                        amount = Integer.parseInt(arguments.next());
                    } else throw new IllegalFormatFlagsException("Multi size-operating flags written");
                    break;
                case "-c":
                    if (splitType == null) {
                        splitType = Type.CHAR;
                        amount = Integer.parseInt(arguments.next());
                    } else throw new IllegalFormatFlagsException("Multi size-operating flags written");
                    break;
                case "-n":
                    if (splitType == null) {
                        splitType = Type.FILE;
                        amount = Integer.parseInt(arguments.next());
                    } else throw new IllegalFormatFlagsException("Multi size-operating flags written");
                    break;
                //Флаг -o, который говорит об имени выходного файла
                case "-o":
                    String value = arguments.next();
                    if (value.equals("-"))
                        useFilename = true;
                    else
                        splittedFilename = value;
                    break;
                default:
                    //Если это не флаг, значит это имя исходного файла, и если оно уже задано - кидается ошибка (по условию)
                    if (filename == null) {
                        filename = arg;
                    } else throw new IllegalFormatFlagsException("Multi filenames written");
                    break;
            }
        }
    }

    //Типы (способы) разделения файла на части
    enum Type {
        LINE, CHAR, FILE
    }
}
