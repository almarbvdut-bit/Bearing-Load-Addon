::: {align="center"}
# Bearing Load Addon

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-2ea043?style=for-the-badge)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Загрузчик-NeoForge-orange?style=for-the-badge)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-blue?style=for-the-badge)](https://modrinth.com/mod/create)
[![Create:
Aeronautics](https://img.shields.io/badge/Create-Aeronautics-blueviolet?style=for-the-badge)](https://github.com/)
:::

------------------------------------------------------------------------

## ✨ О моде

## ⚙️ Как это работает?

Аддон изменяет расчёт нагрузки на поворотном подшипнике. Управление
вращением и остальные механики остаются за исходными модами.

## 📦 Требования

  Компонент             Версия
  --------------------- ------------------------------------------
  Minecraft             `1.21.1`
  NeoForge              Версия, указанная в конфигурации проекта
  Create                `6.0.10`
  Sable                 `2.0.5`
  Create: Aeronautics   `1.3.2`

Установи необходимые моды в тот же экземпляр Minecraft. Совместимость с
другими версиями пока не гарантируется.

## 🚀 Установка

1.  Установи **Minecraft 1.21.1** с подходящей версией NeoForge.
2.  Установи необходимые зависимости, перечисленные выше.
3.  Скачай `.jar` аддона из раздела **Releases**, если готовый релиз
    опубликован.
4.  Помести `.jar` в папку `mods` своего игрового экземпляра.
5.  Запусти игру. Если возникнет ошибка, проверь игровой лог.

Если готового релиза пока нет, аддон можно собрать из исходного кода ---
инструкция ниже.

## 🛠️ Сборка из исходного кода

Для сборки понадобятся Git и совместимая версия JDK для используемой
версии Minecraft и NeoForge.

``` bash
git clone https://github.com/almarbvdut-bit/Bearing-Load-Addon.git
cd Bearing-Load-Addon
```

**Windows:**

``` bat
gradlew.bat build
```

**Linux / macOS:**

``` bash
./gradlew build
```

Если сборка завершится успешно, готовый файл мода можно найти в папке
`build/libs/`.

## 🧪 Тестирование

При проверке аддона сравнивай поведение подшипника с конструкциями
разной массы. Для корректного сравнения используй одинаковые версии
Minecraft, NeoForge, Create и Create: Aeronautics.

Совместимость со всеми дополнениями Create и всеми сочетаниями версий не
гарантируется.

## 🐛 Сообщить об ошибке или предложить изменение

-   Создай [Issue на
    GitHub](https://github.com/almarbvdut-bit/Bearing-Load-Addon/issues).
-   Укажи версии Minecraft, NeoForge, Create и Create: Aeronautics.
-   Приложи соответствующую часть файла `latest.log` и опиши, как
    повторить проблему.

Предложения по улучшению и Pull Request приветствуются.

## 📄 Лицензия

Перед повторным использованием или распространением проекта проверь
наличие файла `LICENSE` в репозитории. Если лицензия не указана,
автоматически считать код свободным для использования нельзя.
