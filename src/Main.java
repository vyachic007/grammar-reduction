import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Ввод терминалов
        System.out.println("Введите терминалы через пробел:");
        String[] terminalInput = scanner.nextLine().trim().split("\\s+");

        Set<Character> terminals = new LinkedHashSet<>();

        for (String s : terminalInput) {
            if (!s.isEmpty()) {
                terminals.add(s.charAt(0));
            }
        }

        // Ввод нетерминалов
        System.out.println("Введите нетерминалы через пробел:");
        String[] nonTerminalInput = scanner.nextLine().trim().split("\\s+");

        Set<Character> nonTerminals = new LinkedHashSet<>();

        for (String s : nonTerminalInput) {
            if (!s.isEmpty()) {
                nonTerminals.add(s.charAt(0));
            }
        }

        // Ввод начального символа
        System.out.println("Введите начальный символ:");
        char start = scanner.nextLine().trim().charAt(0);

        // Ввод количества строк с правилами
        System.out.println("Введите количество строк с правилами:");
        int count = Integer.parseInt(scanner.nextLine().trim());

        Map<Character, List<String>> rules = new LinkedHashMap<>();

        System.out.println("Введите правила, например: S -> aAB|E");

        for (int i = 0; i < count; i++) {

            String line = scanner.nextLine().replace(" ", "");

            String[] parts = line.split("->");

            char left = parts[0].charAt(0);
            String[] rightParts = parts[1].split("\\|");

            rules.put(left, new ArrayList<>(Arrays.asList(rightParts)));
        }

        System.out.println("\nИсходная грамматика:");
        printRules(rules);

        // 1. УДАЛЕНИЕ БЕСПЛОДНЫХ СИМВОЛОВ

        System.out.println("\n1. Удаление бесплодных символов");

        Set<Character> good = new LinkedHashSet<>();

        System.out.println("Y0 = " + good);

        int step = 1;

        while (true) {

            Set<Character> newGood = new LinkedHashSet<>(good);

            for (char left : nonTerminals) {

                List<String> rightParts = rules.get(left);

                if (rightParts == null) {
                    continue;
                }

                for (String right : rightParts) {

                    if (canGenerate(right, terminals, good)) {
                        newGood.add(left);
                        break;
                    }
                }
            }

            System.out.println("Y" + step + " = " + newGood);

            if (newGood.equals(good)) {
                break;
            }

            good = newGood;
            step++;
        }

        // Находим бесплодные нетерминалы
        Set<Character> fruitless = new LinkedHashSet<>(nonTerminals);
        fruitless.removeAll(good);

        System.out.println("\nБесплодные символы: " + fruitless);

        // Создаем грамматику без бесплодных символов
        Map<Character, List<String>> afterFruitless = new LinkedHashMap<>();

        for (char left : nonTerminals) {

            if (!good.contains(left)) {
                continue;
            }

            List<String> rightParts = rules.get(left);

            if (rightParts == null) {
                continue;
            }

            List<String> newRules = new ArrayList<>();

            for (String right : rightParts) {

                boolean allowed = true;

                for (char c : right.toCharArray()) {

                    if (nonTerminals.contains(c) && !good.contains(c)) {
                        allowed = false;
                        break;
                    }
                }

                if (allowed) {
                    newRules.add(right);
                }
            }

            if (!newRules.isEmpty()) {
                afterFruitless.put(left, newRules);
            }
        }

        System.out.println("\nГрамматика после удаления бесплодных:");
        printRules(afterFruitless);

        // 2. УДАЛЕНИЕ НЕДОСТИЖИМЫХ СИМВОЛОВ

        System.out.println("\n2. Удаление недостижимых символов");

        Set<Character> reachable = new LinkedHashSet<>();
        reachable.add(start);

        System.out.println("V0 = " + reachable);

        step = 1;

        while (true) {

            Set<Character> newReachable = new LinkedHashSet<>(reachable);

            for (char left : afterFruitless.keySet()) {

                if (!reachable.contains(left)) {
                    continue;
                }

                for (String right : afterFruitless.get(left)) {

                    for (char c : right.toCharArray()) {

                        if (terminals.contains(c)
                                || afterFruitless.containsKey(c)) {

                            newReachable.add(c);
                        }
                    }
                }
            }

            System.out.println("V" + step + " = " + newReachable);

            if (newReachable.equals(reachable)) {
                break;
            }

            reachable = newReachable;
            step++;
        }

        // Собираем все символы грамматики после удаления бесплодных
        Set<Character> allSymbols = new LinkedHashSet<>();

        // Добавляем оставшиеся нетерминалы
        allSymbols.addAll(afterFruitless.keySet());

        // Добавляем исходные терминалы
        allSymbols.addAll(terminals);

        // Находим все недостижимые символы:
        // и нетерминалы, и терминалы
        Set<Character> unreachable = new LinkedHashSet<>(allSymbols);
        unreachable.removeAll(reachable);

        System.out.println("\nНедостижимые символы: " + unreachable);

        // Формируем итоговую грамматику
        Map<Character, List<String>> result = new LinkedHashMap<>();

        for (char left : afterFruitless.keySet()) {

            if (!reachable.contains(left)) {
                continue;
            }

            List<String> newRules = new ArrayList<>();

            for (String right : afterFruitless.get(left)) {

                boolean allowed = true;

                for (char c : right.toCharArray()) {

                    if ((terminals.contains(c)
                            || afterFruitless.containsKey(c))
                            && !reachable.contains(c)) {

                        allowed = false;
                        break;
                    }
                }

                if (allowed) {
                    newRules.add(right);
                }
            }

            if (!newRules.isEmpty()) {
                result.put(left, newRules);
            }
        }

        System.out.println("\nИтоговая грамматика:");
        printRules(result);

        // Выводим итоговые множества символов
        Set<Character> resultNonTerminals = new LinkedHashSet<>();

        for (char c : afterFruitless.keySet()) {
            if (reachable.contains(c)) {
                resultNonTerminals.add(c);
            }
        }

        Set<Character> resultTerminals = new LinkedHashSet<>();

        for (char c : terminals) {
            if (reachable.contains(c)) {
                resultTerminals.add(c);
            }
        }

        System.out.println("\nИтоговые терминалы: " + resultTerminals);
        System.out.println("Итоговые нетерминалы: " + resultNonTerminals);
        System.out.println("Начальный символ: " + start);

        scanner.close();
    }


    // Проверяет, может ли правая часть правила
    // привести к цепочке из терминалов
    static boolean canGenerate(
            String right,
            Set<Character> terminals,
            Set<Character> good) {

        for (char c : right.toCharArray()) {

            if (!terminals.contains(c) && !good.contains(c)) {
                return false;
            }
        }

        return true;
    }


    // Вывод правил грамматики
    static void printRules(Map<Character, List<String>> rules) {

        for (char left : rules.keySet()) {

            System.out.print(left + " -> ");

            List<String> rightParts = rules.get(left);

            for (int i = 0; i < rightParts.size(); i++) {

                System.out.print(rightParts.get(i));

                if (i < rightParts.size() - 1) {
                    System.out.print(" | ");
                }
            }

            System.out.println();
        }
    }
}