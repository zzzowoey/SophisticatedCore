package net.p3pp3rf1y.sophisticatedcore.common.compontents;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import javax.annotation.Nullable;

import static net.p3pp3rf1y.sophisticatedcore.SophisticatedCoreComponents.ITEM_HANDLER;

public interface IComponentWrapper<T> extends Component {
    IComponentWrapper<Void> EMPTY = new EmptyComponentWrapper<>();
    static <T> IComponentWrapper<T> empty() { return EmptyComponentWrapper.EMPTY.cast(); }

    static IComponentWrapper<?> get(Object provider) {
        return ITEM_HANDLER.get(provider);
    }
    static <X> LazyOptional<IComponentWrapper<X>> maybeGet(Object provider) {
        return LazyOptional.of(() -> ITEM_HANDLER.getNullable(provider)).cast();
    }

    default <X> IComponentWrapper<X> cast() {
        return (IComponentWrapper<X>) this;
    }

    @Nullable
    T get();

    LazyOptional<T> getWrapped();

    default void invalidate() {}

    abstract class SimpleComponentWrapper<T, C> implements IComponentWrapper<T>, TransientComponent {
        protected C object;
        protected LazyOptional<T> wrapped;

        public SimpleComponentWrapper(C object) {
            this.object = object;
        }

        public void invalidate() {
            if (wrapped != null) {
                LazyOptional<T> temp = wrapped;
                wrapped = null;
                temp.invalidate();
            }
        }
    }

    class EmptyComponentWrapper<T> implements IComponentWrapper<T>, TransientComponent {
        @Override
        public T get() {
            return null;
        }

        @Override
        public LazyOptional<T> getWrapped() {
            return LazyOptional.empty();
        }
    }
}
