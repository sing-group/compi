Custom runners
**************

What are Compi `runners`
========================

By default, compi runs task code by spawning local processes. With `runners`,
task' codes are passed to custom-made scripts which are in charge of running
them, for example, by submitting a job to a queue.

Creating a custom runner
========================
You need to write an XML where you define runners.