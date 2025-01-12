package timetravel

import com.arkivanov.mvikotlin.rx.Disposable
import com.arkivanov.mvikotlin.rx.observer
import com.arkivanov.mvikotlin.timetravel.TimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.TimeTravelState
import com.arkivanov.mvikotlin.timetravel.controller.timeTravelController
import com.ccfraser.muirwik.components.MDrawerAnchor
import com.ccfraser.muirwik.components.mDrawer
import com.ccfraser.muirwik.components.themeContext
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.setState
import root.App.TodoStyles.debugButtonsContainerStyle
import root.App.TodoStyles.debugDrawerStyle
import root.App.TodoStyles.eventsContainerStyle
import styled.css
import styled.styledDiv

class TimeTravelComponent(prps: TimeTravelComponentProps) :
    RComponent<TimeTravelComponentProps, TimeTravelComponentState>(prps) {

    private var disposable: Disposable? = null

    init {
        state = js("{}") as TimeTravelComponentState
        state.events = listOf()
        state.mode = TimeTravelState.Mode.IDLE
        state.selectedEventIndex = -1
        state.dialogOpen = false
        state.currentEvent = null
    }

    override fun componentDidMount() {
        disposable = timeTravelController.states(observer { updateState(it) })
    }

    override fun RBuilder.render() {
        themeContext.Consumer { theme ->
            mDrawer(
                open = true,
                onClose = { setState { props.onClose() } },
                anchor = MDrawerAnchor.right
            ) {
                styledDiv {
                    css(debugDrawerStyle)
                    styledDiv {
                        css(eventsContainerStyle)
                        timeTravelEventsView(
                            events = state.events,
                            selectedEventIndex = state.selectedEventIndex,
                            onDebugEventClick = { timeTravelController.debugEvent(eventId = it) },
                            onItemClick = { showDialog(it) }
                        )
                    }
                    styledDiv {
                        css(debugButtonsContainerStyle)
                        timeTravelButtons(
                            mode = state.mode,
                            onRecordClick = { timeTravelController.startRecording() },
                            onStopClick = { timeTravelController.stopRecording() },
                            onMoveToStartClick = { timeTravelController.moveToStart() },
                            onStepBackwardClick = { timeTravelController.stepBackward() },
                            onStepForwardClick = { timeTravelController.stepForward() },
                            onMoveToEndClick = { timeTravelController.moveToEnd() },
                            onCancelClick = { timeTravelController.cancel() }
                        )
                    }
                }
            }
            state.currentEvent?.let { event ->
                infoDialog(
                    open = state.dialogOpen == true,
                    event = event,
                    onClose = { hideDialog() }
                )
            }
        }
    }

    private fun showDialog(event: TimeTravelEvent) {
        setState {
            dialogOpen = true
            currentEvent = event
        }
    }

    private fun hideDialog() {
        setState {
            dialogOpen = false
            currentEvent = null
        }
    }

    private fun updateState(it: TimeTravelState) {
        setState {
            events = it.events
            selectedEventIndex = it.selectedEventIndex
            mode = it.mode
        }
    }

    override fun componentWillUnmount() {
        disposable?.dispose()
        disposable = null
    }
}

external interface TimeTravelComponentProps : RProps {
    var onClose: () -> Unit
}

external interface TimeTravelComponentState : RState {
    var events: List<TimeTravelEvent>
    var mode: TimeTravelState.Mode
    var selectedEventIndex: Int
    var dialogOpen: Boolean?
    var currentEvent: TimeTravelEvent?
}

fun RBuilder.timeTravel(onClose: () -> Unit): ReactElement =
    child(TimeTravelComponent::class) {
        attrs.onClose = onClose
    }
