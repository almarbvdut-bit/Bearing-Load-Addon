import pypandoc, os

markdown_ru = """<div align="center">

# Bearing Load Addon

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-2ea043?style=for-the-badge)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-orange?style=for-the-badge)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-blue?style=for-the-badge)](https://modrinth.com/mod/create)
[![Create: Aeronautics](https://img.shields.io/badge/Create%3A%20Aeronautics-1.3.2-blueviolet?style=for-the-badge)](https://github.com/)

</div>

---

## ⚙️ Как это работает?

Аддон изменяет расчёт нагрузки на подшипнике. Управление вращением и остальные механики остаются за исходными модами.

## 📦 Требования

| Компонент | Точная версия |
|---|---|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.x` |
| Create | `6.0.10` |
| Sable | `2.0.5` |
| Create: Aeronautics | `1.3.2` |

Для работы используй указанные версии зависимостей. Совместимость с другими версиями не гарантируется.

## 🚀 Установка

1. Установи **Minecraft 1.21.1** и **NeoForge 21.1.x**.
2. Установи **Create 6.0.10**, **Sable 2.0.5** и **Create: Aeronautics 1.3.2**.
3. Скачай `.jar` аддона из раздела **Releases**, если готовый релиз опубликован.
4. Помести `.jar` в папку `mods` своего игрового экземпляра.
5. Запусти игру. Если возникнет ошибка, проверь игровой лог.

Если готового релиза пока нет, аддон можно собрать из исходного кода.

## 🛠️ Сборка из исходного кода

Для сборки понадобятся Git и совместимая версия JDK.

```bash
git clone https://github.com/almarbvdut-bit/Bearing-Load-Addon.git
cd Bearing-Load-Addon
```

**Windows:**
```bat
gradlew.bat build
```

**Linux / macOS:**
```bash
./gradlew build
```

После успешной сборки файл мода появится в папке `build/libs/`.

## 🧪 Тестирование

Для проверки используй указанные версии Minecraft, NeoForge, Create, Sable и Create: Aeronautics. Совместимость с другими версиями и дополнениями не гарантируется.

## 💬 Связь

По вопросам, сообщениям об ошибках и предложениям по улучшению пиши мне в Discord: **@almar_b_v_d**.
"""
out = "/mnt/data/Bearing-Load-Addon-README-RU.md"
pypandoc.convert_text(markdown_ru, to="md", format="md", outputfile=out, extra_args=["--standalone"])
print(f"Готово: {out} ({os.path.getsize(out)} байт)")
