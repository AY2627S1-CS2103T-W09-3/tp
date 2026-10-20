---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

--------------------------------------------------------------------------------------------------------------------

## Main window layout checks

### Automated logical viewport checks

Run `./gradlew test --tests seedu.address.ui.MainWindowTest` (Windows: `gradlew test --tests seedu.address.ui.MainWindowTest`).

The `layout_supportedLogicalViewports_keepsMainRegionsVisible` test resizes the JavaFX root to
1920 × 1080, 1536 × 864, 853 × 480, and 820 × 440 logical pixels. It checks that the main regions remain
inside the viewport and that the detail pane displays `Select a client to see details.`.

This is a logical-size check, **not an operating-system display-scaling check**. Resizing the root does not
change the display scale or font metrics. The manual checks below are still required.

### Detail panel checks

Run `./gradlew test --tests seedu.address.ui.DetailPaneTest --tests seedu.address.ui.MainWindowTest`.
The panel tests use real client fields to check formatting, optional-row visibility, switching clients,
clearing the panel, and wrapping and scrolling a 500-character note.
The main-window tests check the embedded panel at 853 × 480 and 820 × 440 logical pixels.

These tests populate the panel through `setPerson`; row-click and `view` selection wiring belongs to issue #101.
The Windows display-scaling results in
[issue #123](https://github.com/AY2627S1-CS2103T-W09-3/tp/issues/123) apply to the original PR #112 build,
commit `4c0c4e47`, before this detail panel was added. They do not verify the populated panel.
Before completing [issue #96](https://github.com/AY2627S1-CS2103T-W09-3/tp/issues/96), check that a
500-character note wraps and scrolls without clipping at an actual 1280 × 720 resolution with 150% display scaling.
Record the tested commit and results separately from the earlier build's results.

### Manual display-scaling checks

Use a display and operating system that support each configuration below. Set the actual screen resolution
and display scaling in the operating system; resizing the app window is not a substitute.
If a configuration is unavailable on your computer, ask a teammate with a supporting display to check it.
Leave unavailable or untested configurations unverified.

For each configuration:

1. Use a separate test-data folder so that your normal client data is not changed.
1. Reopen the app and check both its opening layout and its maximised layout within the available desktop area.
1. Check that the menu, command box, and result box are visible and usable.
1. Check that the client list and detail pane appear side by side, with
   `Select a client to see details.` visible before selecting a client.
1. Ensure there are enough test clients to scroll, then check that the last client can be reached.
1. Check that the footer's data path and client count are visible without overlapping.
   Confirm that the count updates after `add`, `find`, `list` and `delete`.
1. Enter `list` and check that its result is readable. Confirm that no controls overlap or are cut off.
1. Open the Help window and confirm that it and its controls are reachable.
1. Record the app commit, operating system, screen resolution, actual display scale, pass/fail result,
   and a screenshot. Restore your usual display settings afterwards.

### Recorded manual results

* **Tested build:** Original PR #112 test JAR, commit
  [`4c0c4e47`](https://github.com/AY2627S1-CS2103T-W09-3/tp/commit/4c0c4e4785240e8734aa758de43c8de1eb18a3cd).
* **Operating system:** Windows 11.
* **Results confirmed:** 20 October 2026; teammates reported that all manual checks above passed.

| Screen resolution | Actual display scale | Result for the tested build |
| ----------------- | -------------------- | --------------------------- |
| 1920 × 1080 | 100% | Pass |
| 1920 × 1080 | 125% | Pass |
| 1280 × 720 | 150% | Pass |

The configurations and interactive-check results are teammate-reported; no failures were reported.
The [test report and three labelled app screenshots](https://github.com/AY2627S1-CS2103T-W09-3/tp/issues/123#issuecomment-6288082959)
are attached to issue #123. Display-settings screenshots were not supplied.

These results apply only to the tested commit, not to later card or detail-panel changes.
Repeat affected checks after later UI changes, including the final styling in
[issue #102](https://github.com/AY2627S1-CS2103T-W09-3/tp/issues/102), and record the new tested commit.
