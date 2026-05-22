PATH_TO_FX ?= /usr/lib/jvm/javafx-sdk/javafx-sdk-21.0.2/lib

SRC_DIR := src
OUT_DIR := out/production/projmod
MAIN_CLASS := app.MainApp
JAVA_FX_MODULES := javafx.controls,javafx.swing
SVG_JAR := lib/svgSalamander.jar
MYSQL_JAR ?= lib/mysql-connector-j.jar
CLASSPATH := $(OUT_DIR):$(SVG_JAR)$(if $(MYSQL_JAR),:$(MYSQL_JAR),)
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

.PHONY: build run clean resources check-javafx

build: check-javafx
	mkdir -p $(OUT_DIR)
	javac \
		--module-path "$(PATH_TO_FX)" \
		--add-modules $(JAVA_FX_MODULES) \
		-cp "$(SVG_JAR)$(if $(MYSQL_JAR),:$(MYSQL_JAR),)" \
		-d $(OUT_DIR) \
		$(SOURCES)
	$(MAKE) resources

resources:
	mkdir -p $(OUT_DIR)/app
	cp -r $(SRC_DIR)/app/resources $(OUT_DIR)/app/
	cp $(SRC_DIR)/db.properties $(OUT_DIR)/

run: build
	java \
		--module-path "$(PATH_TO_FX)" \
		--add-modules $(JAVA_FX_MODULES) \
		-cp "$(CLASSPATH)" \
		$(MAIN_CLASS)

clean:
	rm -rf $(OUT_DIR)

check-javafx:
	@test -d "$(PATH_TO_FX)" || { \
		echo "JavaFX SDK not found at: $(PATH_TO_FX)"; \
		echo "Set it with: make run PATH_TO_FX=/path/to/javafx-sdk/lib"; \
		exit 1; \
	}
