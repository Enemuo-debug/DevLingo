package com.example.data

import kotlin.random.Random

object LocalChallengeGenerator {
    private val devOpsQuizzes = listOf(
        GeminiChallenge(
            id = "devops_local_1",
            domain = "DevOps",
            topic = "Docker Layers Optimization",
            question = "Which of the following Dockerfile structures represents the best practice to maximize build cache efficiency?",
            optionA = "COPY . .\nRUN npm install",
            optionB = "RUN npm install\nCOPY . .",
            optionC = "COPY package.json .\nRUN npm install\nCOPY . .",
            optionD = "COPY package.json package-lock.json .\nRUN npm install --only=production\nCOPY . .",
            correctAnswer = "D",
            explanation = "By copying package.json and package-lock.json alone first, Docker caches the layer created by 'RUN npm install'. It only re-installs packages if those dependency manifests change. Opt-in production install further reduces the final image size.",
            lesson = """
                ### 1. Concept Overview
                Docker builds container images sequentially using a layered file system. Each instruction within a Dockerfile (such as `COPY`, `RUN`, or `WORKDIR`) represents a read-only delta block. Docker minimizes compilation overhead through build caching, which bypasses steps if their source ingredients remain untouched.

                ### 2. Architectural Mechanics & Examples
                If any instruction's hash is altered, Docker discards the cache for that line and force-recompiles all future subsequent layers. Consider this naive sample:
                ```dockerfile
                COPY . .
                RUN npm install
                ```
                In this case, editing even a single source file (e.g., `app.js`) invalidates the `COPY . .` block, causing Docker to redundantly re-trigger the heavy network-bound package downloads of `RUN npm install`.

                By copying dependency declarations individually first, we guard the installation cache:
                ```dockerfile
                COPY package*.json ./
                RUN npm install --only=production
                COPY . .
                ```

                ### 3. Summary Cheat Sheet
                • **Sequence Guarding**: Structure variables so that rarely changed operations (manifest updates) sit superior to highly dynamic source modifications.
                • **Layer Concatenation**: Link contiguous commands with `&&` operators to trim auxiliary layer counts.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "devops_local_2",
            domain = "DevOps",
            topic = "Kubernetes Pod Lifecycles",
            question = "What is the primary difference between a LivenessProbe and a ReadinessProbe in Kubernetes?",
            optionA = "LivenessProbe restarts the container; ReadinessProbe triggers Pod scaling.",
            optionB = "LivenessProbe checks if the process is alive to restart it; ReadinessProbe checks if it can receive service traffic.",
            optionC = "ReadinessProbe runs only at startup; LivenessProbe runs continuously.",
            optionD = "There is no difference; they are aliases for the same system API endpoint.",
            correctAnswer = "B",
            explanation = "LivenessProbe determines if a container needs to be restarted. ReadinessProbe determines if a container is ready to accept user requests. Inbound traffic is blocked from pods that fail readiness checks.",
            lesson = """
                ### 1. Concept Overview
                In microservice architectures, knowing when a virtual process is running is separate from knowing when it is healthy and willing to serve requests. Kubernetes manages these distinct states using Probes: automated health checkers configured in Pod manifests.

                ### 2. Architectural Mechanics & Examples
                Each container has distinct runtime requirements:
                • **LivenessProbe**: Determines if a container has entered a deadlocked state. If a liveness probe fails, Kubernetes' control loop forcefully kills and restarts the container.
                • **ReadinessProbe**: Determines if a container is fully ready to accept client traffic. If a readiness probe fails, Kubernetes isolates the pod, detaching it from dynamic Endpoints lists so no client requests hit it.

                A yaml specification exemplifies both hooks:
                ```yaml
                livenessProbe:
                  httpGet:
                    path: /healthz/live
                    port: 8080
                readinessProbe:
                  httpGet:
                    path: /healthz/ready
                    port: 8080
                  initialDelaySeconds: 15
                ```

                ### 3. Summary Cheat Sheet
                • **Liveness Failures**: Trigger physical container restarts. Useful for resolving deadlocks.
                • **Readiness Failures**: Temporarily decouple the container from live routing channels. Useful during initial load surges or heavy caching cycles.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "devops_local_3",
            domain = "DevOps",
            topic = "Infrastructure as Code: Terraform",
            question = "In Terraform, what happens when you run 'terraform apply' with a modified state file that does not match real Cloud infrastructure?",
            optionA = "Terraform always overwrites real resources with state metadata blindly.",
            optionB = "Terraform refreshes the state against real infrastructure before determining the execution plan.",
            optionC = "The command fails immediately due to sha256 checksum mismatches.",
            optionD = "All live resources are deleted and re-created automatically.",
            correctAnswer = "B",
            explanation = "Before creating an execution plan, Terraform queries the cloud provider APIs to refresh state caches and align with actual live infrastructure configurations.",
            lesson = """
                ### 1. Concept Overview
                Infrastructure as Code (IaC) relies on a deterministic mapping between declarative code instructions, a tracking ledger (state file), and physical resources instantiated inside public or private cloud environments. Terraform handles this sync journey in phases.

                ### 2. Architectural Mechanics & Examples
                The core ledger of Terraform is the `.tfstate` file, tracking exact mapping IDs, properties, and relationships. 
                When executing an orchestration command:
                1. **Refresh Phase**: Terraform contacts provider API vectors in real-time, matching resources, and updating state cache buffers to match reality.
                2. **Plan Compilation**: It compares the desired schema declarations (`.tf` files) with the freshly refreshed live attributes.
                3. **Delta execution**: It drafts action proposals (create, update, or safely destroy) to resolve any structural delta gaps.

                ### 3. Summary Cheat Sheet
                • **State Ledgers**: State files represent physical footprints, but live APIs are queried directly to discover manual mutations (out-of-band drifts).
                • **Dry Run Integrity**: Running `terraform plan` previews physical alterations without applying physical side effects.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "devops_local_4",
            domain = "DevOps",
            topic = "CI/CD Deployment Strategies",
            question = "Which deployment strategy involves spinning up a complete new version of an app alongside the old one, verifying it, and then instantly swapping traffic?",
            optionA = "Rolling Update",
            optionB = "Canary Deployment",
            optionC = "Blue-Green Deployment",
            optionD = "Recreate Deployment",
            correctAnswer = "C",
            explanation = "Blue-Green deployment maintains two identical physical environments (Blue is active, Green is idle/staging). Once Green passes full tests, router traffic swaps to Green instantly, minimizing deployment downtime and risk.",
            lesson = """
                ### 1. Concept Overview
                Continuous deployment aims to release software with zero downtime. Transitioning live users from an existing build structure (Version A) to a modern build structure (Version B) is handled through established scheduling architectures.

                ### 2. Architectural Mechanics & Examples
                Let's inspect the distinct configurations:
                • **Blue-Green**: Spines up two identical physical clusters side-by-side. "Blue" is the live Production route, and "Green" is the newly compiled deployment. Once full regression suites pass green on the staging subnet, routers instantly toggle dynamic traffic vectors to "Green", bringing downtime close to zero.
                • **Canary**: Releases changes incrementally. Only 5% of traffic flows to Version B first. If latency metrics, error frequencies, and exceptions remain solid, the deployment footprint scales slowly to 100%.

                ### 3. Summary Cheat Sheet
                • **Blue-Green Strategy**: Quick rollback. If Version B encounters post-release failures, router traffic toggles back to Version A immediately. Requires double cloud environment resources.
                • **Canary Strategy**: Excellent for low-risk testing against live subsets. Requires advanced service-mesh traffic splitting logic.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "devops_local_5",
            domain = "DevOps",
            topic = "Kubernetes Storage",
            question = "How does a PersistentVolumeClaim (PVC) bind to a PersistentVolume (PV) in Kubernetes?",
            optionA = "It matches manually written strictly identical IP addresses.",
            optionB = "Kubernetes control plane matches PVC requests with PV attributes like storage capacity and accessModes.",
            optionC = "PVCs require hardcoded absolute Linux filesystem paths to match the host.",
            optionD = "By assigning identical serial keys supplied by cloud hypervisors.",
            correctAnswer = "B",
            explanation = "Kubernetes automatically binds PVCs to PVs that satisfy the requested size, volume mode, storage class, and access modes (e.g., ReadWriteOnce).",
            lesson = """
                ### 1. Concept Overview
                Pod lifecycles are transient: when a container crashes, any localized files are lost. Building persistent states requires dynamic abstraction layers separating hardware storage from standard processing units.

                ### 2. Architectural Mechanics & Examples
                Kubernetes maps physical and logical volumes using two objects:
                • **PersistentVolume (PV)**: An actual storage asset provisioned by cluster administrators or dynamically created by cloud storage classes.
                • **PersistentVolumeClaim (PVC)**: A user's specification request for storage capacity, disk characteristics, and access properties.

                The control plane searches existing inventories for matching PV profiles. For example, if a PVC claims 5Gi with `accessModes: [ReadWriteOnce]`, the controller pairs it to a compatible unassigned PV matching those characteristics:

                ```yaml
                apiVersion: v1
                kind: PersistentVolumeClaim
                metadata:
                  name: web-app-disk
                spec:
                  accessModes:
                    - ReadWriteOnce
                  resources:
                    requests:
                      storage: 5Gi
                ```

                ### 3. Summary Cheat Sheet
                • **Access Modes**: `ReadWriteOnce` (read-write by a single node), `ReadOnlyMany` (read-only by multiple nodes), and `ReadWriteMany` (read-write by multiple nodes concurrently).
                • **Loose Coupling**: Code inside the Pod references the PVC name, allowing infrastructure components to change under-the-hood without breaking app dependencies.
            """.trimIndent()
        )
    )

    private val unityQuizzes = listOf(
        GeminiChallenge(
            id = "unity_local_1",
            domain = "Unity Game Dev",
            topic = "Coroutines and Memory Allocation",
            question = "Which execution statement in a Unity coroutine should be optimized to prevent garbage collection allocation in an Update-like loop?",
            optionA = "yield return null;",
            optionB = "yield return new WaitForSeconds(0.1f);",
            optionC = "yield break;",
            optionD = "yield return new WaitForEndOfFrame();",
            correctAnswer = "B",
            explanation = "Using 'yield return new WaitForSeconds(0.1f);' allocates a new instance of WaitForSeconds every cycle. Best practice is to cache a single readonly WaitForSeconds instance in a class-level field to prevent GC overhead.",
            lesson = """
                ### 1. Concept Overview
                Unity runs on a managed heap environment. Allocating memory dynamically triggers downstream Garbage Collector (GC) cycles, causing noticeable micro-stutters during intensive gameplay. In Unity, Coroutines are used for cooperative multitasking, but how we write `yield` instructions determines their memory footprint.

                ### 2. Architectural Mechanics & Examples
                Consider this commonly written but inefficient coroutine block:
                ```csharp
                IEnumerator QueryServerRepeatedly() {
                    while (true) {
                        // Bad Practice: Instantiates a new WaitForSeconds heap object each loop!
                        yield return new WaitForSeconds(0.1f);
                        PollNetworkUpdate();
                    }
                }
                ```
                Because this runs ten times per second, it continuously dumps `WaitForSeconds` objects into the heap.

                To optimize this, cache the wait instruction at the class level:
                ```csharp
                private readonly WaitForSeconds delay = new WaitForSeconds(0.1f);

                IEnumerator OptimizedQuery() {
                    while (true) {
                        // Good Practice: Reuses the exact same cached reference
                        yield return delay;
                        PollNetworkUpdate();
                    }
                }
                ```

                ### 3. Summary Cheat Sheet
                • **GC Allocations**: Heap allocations build up pressure. Instantiating objects in frequent loop routines triggers GC lag.
                • **Zero-Allocation yield**: Cache instances of `WaitForSeconds`, `WaitForEndOfFrame`, and `WaitForFixedUpdate` wherever repetitive polling is needed.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "unity_local_2",
            domain = "Unity Game Dev",
            topic = "Physics and FixedUpdate",
            question = "Why must rigidbodies be moved or manipulated inside FixedUpdate() rather than Update() in Unity?",
            optionA = "Update() is faster, which will make rigidbody collisions clip through walls.",
            optionB = "FixedUpdate runs at a consistent, fixed interval independent of frame rate, keeping physics math predictable.",
            optionC = "Coroutine schedules do not support accessing Unity Physics parameters within Update().",
            optionD = "FixedUpdate compiles on separate graphics card threads.",
            correctAnswer = "B",
            explanation = "Because frame rates fluctuate dynamically in Update(), physics simulations applied there would behave inconsistently. FixedUpdate runs on constant tick rates aligned with Unity's internal physics engine cycles.",
            lesson = """
                ### 1. Concept Overview
                Game loop loops comprise visual rendering phases (`Update()`) and physical calculation phases (`FixedUpdate()`). Dynamic updates handle rendering, user input, and timing variations. Fixed update schedules handle physics simulation tick updates to ensure deterministic velocity, forces, and collision outcomes.

                ### 2. Architectural Mechanics & Examples
                Let's inspect the direct structural divergence:
                • **Update()**: Executed exactly once per frame. This varies wildly based on performance load. Calculating forces on a rigidbody here will make characters jitter, speed up during lag spikes, or clip through physical colliders.
                • **FixedUpdate()**: Executed at a fixed interval (default is 0.02 seconds, or 50Hz in Unity project target configurations) regardless of screen refresh rates. 

                ```csharp
                // Best Practice for Physics:
                void FixedUpdate() {
                    Vector3 forceDirection = new Vector3(1, 0, 0);
                    rb.AddForce(forceDirection * moveSpeed, ForceMode.Acceleration);
                }
                ```

                ### 3. Summary Cheat Sheet
                • **Physics Ticks**: Always place inputs/keys tracking in `Update()`, but apply physical movements (`Rigidbody.velocity`, `AddForce`) in `FixedUpdate()`.
                • **Time Multiplying**: In `FixedUpdate()`, use `Time.fixedDeltaTime` instead of standard `Time.deltaTime`.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "unity_local_3",
            domain = "Unity Game Dev",
            topic = "ScriptableObjects",
            question = "What is the primary benefit of using standard ScriptableObjects in a Unity game architecture?",
            optionA = "They compile 4x faster than typical MonoBehaviours.",
            optionB = "They store shared immutable data independently of active scenes, drastically reducing scene instance memory duplicates.",
            optionC = "They possess high-priority rendering callbacks for custom vertex shaders.",
            optionD = "They auto-serialize game saves into JSON on mobile devices automatically.",
            correctAnswer = "B",
            explanation = "ScriptableObjects store data asset templates. Instead of every game instance duplicating asset stats, they reference a single ScriptableObject file, resulting in massive runtime memory savings.",
            lesson = """
                ### 1. Concept Overview
                When deploying 10,000 enemy prefab actors inside a high-performance scene, attaching a script with independent fields (such as `maxHealth`, `baseSpeed`, `attackPower`) results in redundant memory storage because every single instance copies those variables. ScriptableObjects solve this by keeping asset data decoupled from real scene tree instances.

                ### 2. Architectural Mechanics & Examples
                A ScriptableObject acts as an independent global asset catalog:
                ```csharp
                [CreateAssetMenu(fileName = "EnemyConfig", menuName = "Stats/Enemy")]
                public class EnemyStats : ScriptableObject {
                    public float maxHealth;
                    public float baseSpeed;
                }
                ```
                Instead of storing these configurations on a GameObject script, the MonoBehaviour references the common asset directly:
                ```csharp
                public class EnemyController : MonoBehaviour {
                    public EnemyStats config; // Single references to shared read-only template
                    private float currentHealth;

                    void Start() {
                        currentHealth = config.maxHealth;
                    }
                }
                ```

                ### 3. Summary Cheat Sheet
                • **Single Instance Memory**: If 500 instances reference the same ScriptableObject configuration template, memory allocations are shared, avoiding redundant instantiations.
                • **Asset Persistence**: Data modifications to ScriptableObjects made in the Unity Editor during play-mode do not reset when exiting play-mode.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "unity_local_4",
            domain = "Unity Game Dev",
            topic = "Vector Mathematics",
            question = "Which vector operation is best suited to calculate the direct angle of alignment between a player's forward vector and an aiming enemy?",
            optionA = "Vector3.Cross",
            optionB = "Vector3.Dot",
            optionC = "Vector3.Lerp",
            optionD = "Vector3.Scale",
            correctAnswer = "B",
            explanation = "The Dot product returns 1 if vectors align completely, 0 if perpendicular, and -1 if pointing in opposite directions. It is the cheapest mathematical check for visual checking and direct facing calculations.",
            lesson = """
                ### 1. Concept Overview
                Computing spatial relations is key for AI behaviors, stealth detection, and mechanics. While Euler angles require costly trigonometric checks, vector algebra enables simple execution paths through Dot and Cross Products.

                ### 2. Architectural Mechanics & Examples
                Let's distinguish the primary calculations:
                • **Dot Product (`Vector3.Dot`)**: Multiplies two vectors, outputting a scalar float. Returns:
                  - `1.0`: Vectors point in strictly matching directions.
                  - `0.0`: Vectors are perpendicular ($90^\circ$).
                  - `-1.0`: Vectors point in completely opposite directions.
                • **Cross Product (`Vector3.Cross`)**: Outputs a third perpendicular vector pointing outwards. Excellent for calculating torque angles or surface normals.

                ```csharp
                // Sneak detection algorithm:
                float alignment = Vector3.Dot(enemy.forward, playerDirection.normalized);
                if (alignment > 0.8f) {
                    Debug.Log("Player visual spotted inside narrow visual cone!");
                }
                ```

                ### 3. Summary Cheat Sheet
                • **Dot Product Usage**: Perfect for field-of-view (FOV) checks, threat detection, and direction alignment checks.
                • **Cross Product Usage**: Perfect for rotation alignments, right-hand-rule coordinate generation, and game physics.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "unity_local_5",
            domain = "Unity Game Dev",
            topic = "Garbage Collection & Strings",
            question = "Which practice generates a constant stream of garbage collector allocations at runtime in Unity UI loops?",
            optionA = "Combining strings with code such as: statusText.text = \"Score: \" + currentScore;",
            optionB = "Passing structures (struct) by value across function calls.",
            optionC = "Invoking Camera.main once and keeping a reference cached.",
            optionD = "Using custom arrays instead of dynamic lists.",
            correctAnswer = "A",
            explanation = "Strings are immutable. Concatenating strings with the '+' operator allocates a brand new string instance on the managed heap every frame, causing frame stutters due to GC collections.",
            lesson = """
                ### 1. Concept Overview
                In string programming, strings are immutable arrays of char blocks. Each string operation—such as appending texts or executing custom replacements—creates a distinct reference object on the managed heap. Appending values inside active game frames generates heap waste fast, causing background GC sweeps that slow down gameplay.

                ### 2. Architectural Mechanics & Examples
                Observe this common visual HUD loop:
                ```csharp
                // Bad Practice: run inside Update() at 60fps
                void Update() {
                    statusText.text = "HP: " + currentHealth.ToString() + " / " + maxHealth.ToString();
                }
                ```
                The '+' operators and individual `.ToString()` calls construct five intermediate string items on the heap every single frame, causing garbage collection pauses.

                Instead, cache the numerical increments, and update UI widgets only when health values actually change, or utilize a `System.Text.StringBuilder` buffer.

                ### 3. Summary Cheat Sheet
                • **String Immutability**: Appending generates heap garbage. Update UI strings strictly on event changes, not per-frame in `Update()`.
                • **String Builders**: Use `StringBuilder` to format strings without intermediate heap garbage, or use specialized string management packages.
            """.trimIndent()
        )
    )

    private val nodeJsQuizzes = listOf(
        GeminiChallenge(
            id = "node_local_1",
            domain = "Node.js (JS & TS)",
            topic = "Runtime Event Loop",
            question = "In Node.js event-driven runtime, which queue possesses the absolute highest callback execution priority?",
            optionA = "setImmediate()",
            optionB = "setTimeout()",
            optionC = "process.nextTick()",
            optionD = "I/O Poll Queue",
            correctAnswer = "C",
            explanation = "The process.nextTick() queue is processed immediately after the current operation finishes executing, BEFORE the event loop shifts into its phase cycles.",
            lesson = """
                ### 1. Concept Overview
                Node.js is built on a single-threaded event loop architecture using the V8 engine and the libuv C++ library. While other languages spin up multiple operating system threads for parallel scaling, Node.js manages thousands of connections on a single thread through asynchronous task queuing.

                ### 2. Architectural Mechanics & Examples
                The Event Loop processes operations on specific phased queues:
                • **Timers**: Executes expired `setTimeout` and `setInterval` callbacks.
                • **Pending/Poll**: Executes I/O tasks or reads network buffers.
                • **Check**: Executes `setImmediate` callback segments.

                However, there are two distinct high-priority queue structures processed *immediately* after the active JS execution stacks finish, before the loop ticks to any of the above phases:
                1. `process.nextTick()` (Absolute highest priority)
                2. `Promise` microtask queue (Next highest priority)

                ```javascript
                setTimeout(() => console.log("Timer Call"), 0);
                setImmediate(() => console.log("Check Call"));
                process.nextTick(() => console.log("NextTick Call"));
                console.log("Synchronous Call");
                ```
                Output Sequence:
                1. "Synchronous Call"
                2. "NextTick Call"
                3. "Timer Call"
                4. "Check Call"

                ### 3. Summary Cheat Sheet
                • **Phase Loops**: Avoid blocking processing threads.
                • **Call Ordering**: Synchronous code executes first, then `nextTick`, then Promises, followed by Phase loop tasks (timers, then immediate).
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "node_local_2",
            domain = "Node.js (JS & TS)",
            topic = "TypeScript Advanced Generics",
            question = "In TypeScript, what utility type allows you to construct a brand new type by making all fields in an existing type optional?",
            optionA = "Required<T>",
            optionB = "Partial<T>",
            optionC = "Pick<T, K>",
            optionD = "Record<K, T>",
            correctAnswer = "B",
            explanation = "Partial<T> wraps a type T and converts all of its properties into optional flags (property?), making it incredibly useful for state updates or partial database records.",
            lesson = """
                ### 1. Concept Overview
                TypeScript provides native Utility Types as functional operators on your object types. Instead of manually declaring duplicates of interfaces to specify different characteristics (such as optional or read-only properties), we write static generic mappings.

                ### 2. Architectural Mechanics & Examples
                Let's inspect the options:
                • `Partial<T>`: Transforms all keys in `T` into optional fields (`?`). This is perfect for patch queries:
                ```typescript
                interface User {
                    id: string;
                    displayName: string;
                    email: string;
                }

                // Generates: { id?: string; displayName?: string; email?: string; }
                function updateProfile(id: string, payload: Partial<User>) {
                    prisma.user.update({ where: { id }, data: payload });
                }
                ```
                • `Required<T>`: Reverses this behavior, making all properties mandatory.
                • `Pick<T, K>`: Selects only a designated list of keys from the interface.

                ### 3. Summary Cheat Sheet
                • **Partial**: Makes fields optional. Excellent for HTTP request body validation.
                • **Type Safety**: Using generic utilities reduces boilerplate code and guarantees that code edits sync automatically across dependent parameters.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "node_local_3",
            domain = "Node.js (JS & TS)",
            topic = "Stream Architectures",
            question = "Why should we prefer streams (such as fs.createReadStream) over fs.readFile when reading massive 10GB log files?",
            optionA = "readFile will crash because it tries to buffer the entire file into V8 RAM at once.",
            optionB = "Streams compress standard files automatically in memory during execution.",
            optionC = "fs.readFile is deprecated on modern Node.js versions.",
            optionD = "Streams use separate clusters automatically without coding.",
            correctAnswer = "A",
            explanation = "fs.readFile loads the complete target file into memory, exceeding V8 Heap allocation ceilings. Streams read files in chunk buffers, maintaining solid memory allocation regardless of raw file dimensions.",
            lesson = """
                ### 1. Concept Overview
                When handling data records or network transfers, reading raw files entirely into memory becomes a major scaling bottleneck. High-load Node.js apps use Streams to process data in small sequential chunks.

                ### 2. Architectural Mechanics & Examples
                When using `fs.readFile()`, Node.js allocates an in-memory buffer equal to the file size. If three users concurrently request a 1GB file, Node buffers 3GB of RAM. If files exceed V8's heap maximum (typically 1.4GB on 64-bit systems), the server crashes with an Out-of-Memory exception.

                Streams resolve this by maintaining a fixed-size internal memory buffer (typically 64KB for read streams):
                ```javascript
                const fs = require('fs');

                const reader = fs.createReadStream('huge_log.txt');
                const writer = fs.createWriteStream('copy.txt');

                // Pipe manages flow rate (backpressure) automatically
                reader.pipe(writer);
                ```

                ### 3. Summary Cheat Sheet
                • **Buffer Ceilings**: Avoid nesting full file arrays to prevent heap exhaustion under high-load.
                • **Backpressure Rules**: Piping stream components prevents fast read files from flooding slow write streams.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "node_local_4",
            domain = "Node.js (JS & TS)",
            topic = "Error Handling in Async Node",
            question = "What happens if a Promise rejects in an async function but is not caught with try/catch or a catch block in Node.js?",
            optionA = "The runtime retries execution of the function automatically.",
            optionB = "An 'unhandledRejection' event is emitted; in modern Node versions, the process terminates with a non-zero exit code.",
            optionC = "The server returns an HTTP 500 error page to all connections automatically.",
            optionD = "The rejection is stored in a logs file and ignored during runtime.",
            correctAnswer = "B",
            explanation = "Modern Node.js treats unhandled promise rejections as critical runtime violations, crashing the process to prevent memory leaks and zombie thread executions.",
            lesson = """
                ### 1. Concept Overview
                Traditional software uses central exception chains. However, Node's asynchronous model makes it challenging to propagate errors because they can occur outside the active execution context.

                ### 2. Architectural Mechanics & Examples
                If a promise rejects with an error but has no active handler, JavaScript triggers an `unhandledRejection` event. In older versions of Node, these alerts were just warnings. Modern versions treat them as fatal crashes to prevent silent connection dropouts and resource leaks.

                You can listen for these globally to capture logs, but the process should still restart:
                ```javascript
                process.on('unhandledRejection', (reason, promise) => {
                    console.error('CRITICAL: Unhandled Rejection at:', promise, 'reason:', reason);
                    // Crucial: safely flush logs and gracefully restart container
                    process.exit(1);
                });
                ```

                ### 3. Summary Cheat Sheet
                • **Always Handle Rejections**: Wrap async blocks with `try/catch` or use `.catch()` handlers.
                • **Restart Policies**: Let processes crash on uncaught errors, but use a container manager like Docker or PM2 to restart them.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "node_local_5",
            domain = "Node.js (JS & TS)",
            topic = "TypeScript Type Narrowing",
            question = "Which operator should be used to narrow down a union type to check if an object belongs to a given class at runtime?",
            optionA = "typeof",
            optionB = "instanceof",
            optionC = "interfaceCheck",
            optionD = "as KeyOf",
            correctAnswer = "B",
            explanation = "While 'typeof' checks core primitive types (string, number), 'instanceof' inspects prototype chains, verifying if a runtime object is an instance of a class.",
            lesson = """
                ### 1. Concept Overview
                Union types (`A | B`) are powerful, but compiling specific operations requires type narrowing. This validates the runtime type before accessing properties that may not exist on all variants.

                ### 2. Architectural Mechanics & Examples
                TypeScript provides several methods for narrowing:
                • `typeof x === "string"`: Useful for basic JavaScript primitives (string, number, boolean, symbol, undefined).
                • `x instanceof TargetClass`: Inspects the prototype chain at runtime. Ideal for checking if an object was created by a specific class constructor.
                • Custom Type Guards (`is` keyword): Enables user-defined type-checks.

                ```typescript
                class FileLogger { log() { console.log("File"); } }
                class DbLogger { log() { console.log("DB"); } }

                function execute(logger: FileLogger | DbLogger) {
                    if (logger instanceof FileLogger) {
                        // TypeScript narrows 'logger' to FileLogger here
                        logger.log();
                    }
                }
                ```

                ### 3. Summary Cheat Sheet
                • **typeof**: For TypeScript/Javascript primitive checks.
                • **instanceof**: For class constructor checks.
                • **in operator**: Checks for a specific property name on an object.
            """.trimIndent()
        )
    )

    private val aspNetQuizzes = listOf(
        GeminiChallenge(
            id = "aspnet_local_1",
            domain = "ASP.NET",
            topic = "Dependency Lifetimes",
            question = "What is the lifetime behavior of a service registered with Builder.Services.AddScoped<T>() in ASP.NET Core?",
            optionA = "A single instance is shared across the entire application lifetime.",
            optionB = "A new instance is created for every single HTTP request cycle and disposed afterward.",
            optionC = "A new instance is instantiated every time it is injected into a class constructor.",
            optionD = "It represents transient memory that is garbage collected within 30 seconds of non-use.",
            correctAnswer = "B",
            explanation = "AddScoped specifies that an instance is tied to an HTTP request scope. A single object instance serves all classes invoking that service within that single request journey.",
            lesson = """
                ### 1. Concept Overview
                Dependency Injection (DI) is a first-class citizen in ASP.NET Core. When registering dependencies in the IOC Container, managing their lifecycle correctly prevents bugs like capturing dependencies with shorter lifespans or leaking memory.

                ### 2. Architectural Mechanics & Examples
                ASP.NET provides three built-in service lifetimes:
                • **Transient (`AddTransient`)**: A new instance is created every time the service is requested. Ideal for lightweight, stateless components.
                • **Scoped (`AddScoped`)**: A single instance is created per HTTP request scope. Useful for stateful services or database contexts (like EF Core's `DbContext`).
                • **Singleton (`AddSingleton`)**: A single instance is created once and reused across all incoming requests throughout the application's lifetime.

                ```csharp
                // Program.cs
                var builder = WebApplication.CreateBuilder(args);

                builder.Services.AddTransient<ITransientService, SmallService>();
                builder.Services.AddScoped<IScopedService, RequestTracker>();
                builder.Services.AddSingleton<ISingletonService, MasterCache>();
                ```

                ### 3. Summary Cheat Sheet
                • **Scoped Services**: Never inject a Scoped service directly into a Singleton service, as this creates a "Captive Dependency" and leaks memory.
                • **Disposal**: ASP.NET automatically disposes of services at the end of their respective scopes if they implement `IDisposable`.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "aspnet_local_2",
            domain = "ASP.NET",
            topic = "Middleware Execution Order",
            question = "In ASP.NET Core Program.cs, what is the sequence in which middleware executes when handling an HTTP request?",
            optionA = "They execute strictly in the sequential order they are added via app.Use...() calls.",
            optionB = "They execute based on alphabetized method names of custom middlewares.",
            optionC = "Database middlewares always execute first before authorization checks.",
            optionD = "They run concurrently on background threadpools.",
            correctAnswer = "A",
            explanation = "Middleware behaves like a onion-shaped pipeline. Incoming HTTP requests pass through the registered middleware modules strictly in the order they were registered using app.Use...() in code.",
            lesson = """
                ### 1. Concept Overview
                Middlewares are software components assembled into an application pipeline to handle requests and responses. Each component can perform operations before and after the next component in the pipeline.

                ### 2. Architectural Mechanics & Examples
                The pipeline operates like an onion-shaped structure:
                1. Requests travel through the middlewares sequentially (e.g., Logging -> Routing -> Auth -> Endpoint).
                2. Once a middleware executes `next()`, control is handed over to the next middleware.
                3. The final endpoint generates a response, which travels back in the reverse order.

                ```csharp
                // Program.cs
                var app = builder.Build();

                app.UseExceptionHandler("/Error"); // Outer loop
                app.UseStaticFiles();             // Static asset server
                app.UseRouting();                 // Route matching
                app.UseAuthorization();           // Privileges check
                app.MapControllers();             // Final endpoints execution
                ```

                ### 3. Summary Cheat Sheet
                • **Sequential Order**: The registration order in `Program.cs` directly determines the execution order.
                • **Short-Circuiting**: Middleware like static file loaders or authorization filters can immediately return a response without calling `next()`.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "aspnet_local_3",
            domain = "ASP.NET",
            topic = "Entity Framework Core Performance",
            question = "Which EF Core method suffix should be appended to queries to dramatically speed up read-only database fetches?",
            optionA = "AsNoTracking()",
            optionB = "UseReadOptimized()",
            optionC = "ToListImmediate()",
            optionD = "EnableFastMode()",
            correctAnswer = "A",
            explanation = "AsNoTracking tells EF Core to bypass change-tracking mechanisms for query results. Skipping tracking caches lowers memory usage and speeds up read operations immensely.",
            lesson = """
                ### 1. Concept Overview
                Entity Framework Core is a feature-rich Object-Relational Mapper (ORM). By default, EF Core tracks changes to all queried entities, allowing you to easily modify entity fields and call `SaveChanges()` to persist updates. However, this tracking subsystem introduces substantial overhead for read-only workflows.

                ### 2. Architectural Mechanics & Examples
                When EF Core tracks entities, it keeps a copy of their original state and property values in an internal snapshot ledger. For read-only actions, tracking is redundant.

                Appending `.AsNoTracking()` bypasses this tracking layer entirely:
                ```csharp
                // Fast, read-only query:
                var users = await dbContext.Users
                    .AsNoTracking()
                    .Where(u => u.IsActive)
                    .ToListAsync();
                ```

                ### 3. Summary Cheat Sheet
                • **Tracking Off**: Use `AsNoTracking()` for queries where you only display data without modifying database records.
                • **Global Default**: You can configure your dbContext's change tracker to default to no-tracking behavior application-wide for maximum read performance.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "aspnet_local_4",
            domain = "ASP.NET",
            topic = "Routing Constraints",
            question = "How do you specify in a Route attribute that a path parameter must be a valid integer, e.g. /api/users/{id}?",
            optionA = "[HttpGet(\"api/users/{id:int}\")]",
            optionB = "[HttpGet(\"api/users/{id=typeof(int)}\")]",
            optionC = "[HttpGet(\"api/users/{id}\", Constraint = Type.Integer)]",
            optionD = "[HttpGet(\"api/users/{int:id}\")]",
            correctAnswer = "A",
            explanation = "ASP.NET Core routing supports inline route constraints. Adding ':int' ensures the endpoint only matches incoming requests if the route parameter value parses as a standard integer.",
            lesson = """
                ### 1. Concept Overview
                Routing parses incoming HTTP request URIs and dispatches them to matching controller endpoint actions. Route constraints let you filter routes based on parameter type or patterns, avoiding overlapping route conflicts.

                ### 2. Architectural Mechanics & Examples
                Instead of checking parameter types manually inside controllers, you can define inline rules:
                • `{id:int}`: Restricts parameter to integer values.
                • `{id:guid}`: Restricts parameter to globally unique identifier templates.
                • `{username:minlength(3)}`: Requires a minimum string length.

                ```csharp
                [ApiController]
                [Route("api/[controller]")]
                public class UsersController : ControllerBase {
                    // Perfect: Only handles paths like api/users/25
                    [HttpGet("{id:int}")]
                    public IActionResult GetUserById(int id) {
                        return Ok();
                    }
                }
                ```

                ### 3. Summary Cheat Sheet
                • **Inline Rules**: Constraints are declared using `:` inside path curly braces.
                • **Validation**: Constraints validate URL structures; they do not perform complex application-level security checks or business logical validations.
            """.trimIndent()
        ),
        GeminiChallenge(
            id = "aspnet_local_5",
            domain = "ASP.NET",
            topic = "Filters vs. Middleware",
            question = "What is a primary distinction between an Action Filter and a Middleware component in ASP.NET Core?",
            optionA = "Middleware executes inside the ASP.NET routing context; Filters do not.",
            optionB = "Action Filters inspect actions and have access to MVC model binding; Middleware operates at the general HTTP contract abstraction level.",
            optionC = "Filters are restricted only to ASP.NET Razor files.",
            optionD = "Action Filters are strictly compiled into client assemblies.",
            correctAnswer = "B",
            explanation = "Middleware is global across the web framework context. Action Filters execute inside the overall MVC / Controller system, giving them access to action arguments, controller attributes, and model binders.",
            lesson = """
                ### 1. Concept Overview
                Both Middleware and Filters customize request execution. However, they hook into different stages of ASP.NET Core's runtime pipeline.

                ### 2. Architectural Mechanics & Examples
                Let's inspect the distinct boundaries:
                • **Middleware**: Sits outside the MVC Routing system. Sits closer to the network level, processing raw HTTP contexts without any knowledge of Controller actions, parameter bindings, or action descriptors.
                • **Filters**: Execute within the MVC/Routing subsystem. Action Filters run specifically before and after your Controller actions execute, giving you access to route arguments, model states, and response payloads.

                ```csharp
                // Custom Action Filter:
                public class RestrictBetaAttribute : ActionFilterAttribute {
                    public override void OnActionExecuting(ActionExecutingContext context) {
                        // Access parameters or controller details:
                        if (!FeatureGate.BetaEnabled) {
                            context.Result = new BadRequestObjectResult("Beta group closed.");
                        }
                    }
                }
                ```

                ### 3. Summary Cheat Sheet
                • **Middleware Scope**: For global concerns like CORS, routing, HTTPS redirection, and logging.
                • **Filter Scope**: For controller-level features like request validation, transaction management, and formatters.
            """.trimIndent()
        )
    )

    fun getRandomChallenge(domain: String): GeminiChallenge {
        val list = when (domain) {
            "DevOps" -> devOpsQuizzes
            "Unity Game Dev" -> unityQuizzes
            "Node.js (JS & TS)" -> nodeJsQuizzes
            "ASP.NET" -> aspNetQuizzes
            else -> devOpsQuizzes
        }
        return list[Random.nextInt(list.size)]
    }

    fun getAllDomains() = listOf("DevOps", "Unity Game Dev", "Node.js (JS & TS)", "ASP.NET")
}
