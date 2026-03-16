package com.example.customkeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.customkeyboard.ui.theme.CustomKeyboardTheme

// AbstractComposeView only supports being added into view hierarchies propagating LifecycleOwner and SavedStateRegistryOwner
class ItsukiKeyboardService() : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        savedStateRegistryController.savedStateRegistry

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onCreateInputView(): View {
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return ItsukiKeyBoardView(this)
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

// Bridging From Composable to Android View
class ItsukiKeyBoardView(private val service: ItsukiKeyboardService) :
    AbstractComposeView(service) {
    @Composable
    override fun Content() {
        CustomKeyboardTheme {
            ItsukiKeyBoard(service)
        }
    }
}

// Actual Keyboard view
// null-able service for preview
@Preview(showBackground = true)
@Composable
fun ItsukiKeyBoard(service: ItsukiKeyboardService? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EnterTextButton("Hello!", service)
            EnterTextButton("Hey!", service)
            EnterTextButton("What's up!", service)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button({
                // or alternatively, simulate key event.
                // val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
                // service?.currentInputConnection?.sendKeyEvent(event)
                service?.currentInputConnection?.deleteSurroundingText(1, 0)
            }, modifier = Modifier.weight(1f)) {
                Text("Delete")
            }

            Button({
                service?.requestHideSelf(0)
            }, modifier = Modifier.weight(1f)) {
                Text("Done")
            }
        }
    }
}

@Composable
fun RowScope.EnterTextButton(text: String, service: ItsukiKeyboardService?) {
    Button({
        // commitText()
        // Commits a CharSequence to the text field and sets a new cursor position.
        // newCursorPosition: If > 0, this is relative to the end of the text - 1; if <= 0, this is relative to the start of the text. So a value of 1 will always advance the cursor to the position after the full text being inserted.
        service?.currentInputConnection?.commitText(text, 1)
    }, modifier = Modifier.weight(1f)) {
        Text(text)
    }
}
