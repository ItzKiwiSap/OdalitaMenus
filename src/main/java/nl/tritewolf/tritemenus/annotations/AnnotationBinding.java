package nl.tritewolf.tritemenus.annotations;

import nl.tritewolf.tritejection.annotations.TriteJect;
import nl.tritewolf.tritejection.utils.types.TypeReporter;
import nl.tritewolf.tritemenus.patterns.DirectionPattern;
import nl.tritewolf.tritemenus.patterns.IteratorPattern;
import nl.tritewolf.tritemenus.patterns.PatternContainer;
import nl.tritewolf.tritemenus.menu.MenuContainer;
import nl.tritewolf.tritemenus.menu.providers.MenuProvider;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class AnnotationBinding implements TypeReporter {

    @TriteJect
    private PatternContainer patternContainer;

    @TriteJect
    private MenuContainer menuContainer;

    private final List<String> classNames = new ArrayList<>();

    @Override
    public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
        try {
            if (this.classNames.contains(className)) return;

            boolean addClassName = false;
            if (annotation.equals(Menu.class)) {
                addClassName = true;

                Class<?> menuClass = Class.forName(className);
                if (MenuProvider.class.isAssignableFrom(menuClass)) {
                    this.menuContainer.registerMenu((MenuProvider) menuClass.getDeclaredConstructor().newInstance());
                }
            }

            if (annotation.equals(Pattern.class)) {
                addClassName = true;

                Class<?> menuClass = Class.forName(className);
                if (IteratorPattern.class.isAssignableFrom(menuClass)) {
                    this.patternContainer.registerIteratorPattern((IteratorPattern) menuClass.getDeclaredConstructor().newInstance());
                }
                if (DirectionPattern.class.isAssignableFrom(menuClass)) {
                    this.patternContainer.registerDirectionsPattern((DirectionPattern) menuClass.getDeclaredConstructor().newInstance());
                }
            }

            if (addClassName) {
                this.classNames.add(className);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{Menu.class, Pattern.class};
    }
}