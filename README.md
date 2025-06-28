<img src=".github/resources/logo.jpg" />

<h1 align="center">JPA Cache provider ⚡ for Spring 6.0.x+</h1>

<p align="center">
Persistent, high-performance JPA cache provider for Spring Cache. <br/>
Seamlessly integrates with your database, providing TTL, size limits, and eviction strategies out of the box.
</p>

<p align="center">
  <a href="https://search.maven.org/artifact/io.github.admiralxy/jpa-cache-core">
    <img src="https://img.shields.io/maven-central/v/io.github.admiralxy/jpa-cache-core?style=for-the-badge" alt="Maven Central Version"/>
  </a>
  <img src="https://img.shields.io/github/actions/workflow/status/AdmiralXy/spring-cache-jpa/build.yml?style=for-the-badge" alt="GitHub Actions Workflow Status"/>
  <img src="https://img.shields.io/github/license/AdmiralXy/spring-cache-jpa?style=for-the-badge" alt="GitHub License"/>
</p>

<br clear="both"/>

## ✨ Features

* **TTL Support**: Automatic expiration of stale entries.
* **Size Limit & Eviction**: Configure a maximum number of entries per cache region and choose between LRU, LFU, or FIFO eviction policies.
* **Persistent Storage**: Cache survives application restarts and can be shared across multiple instances.
* **Automatic Leader-based Cleanup**: In clustered environments, ensures a single instance removes expired rows to avoid contention.
* **Zero Extra Dependencies**: Only Spring JPA and your existing database driver.

Stop reinventing the wheel — start shipping features! ⭐

---

## 🚀 Quick Start with Spring or Spring Boot

[Quick Start guide on the Wiki](https://github.com/AdmiralXy/spring-cache-jpa/wiki/How-to-use#quick-start)

## ⚙️ Configuration

[Configuration on the Wiki](https://github.com/AdmiralXy/spring-cache-jpa/wiki/How-to-use#configuration)

## 📦 Tested on
| Database             | Version |
|----------------------|---------|
| PostgreSQL           | 15      |
| MySQL                | 8.4     |
| Oracle               | 18 XE   |
| Microsoft SQL Server | 2019    |

## 🌍 Roadmap
* 📊 **Micrometer metrics**

Give the project a ⭐ if you like the vision — it fuels development!

## 🤝 Contributing
1. **Fork** → `git clone` → feature branch.
2. `./gradlew build`.
3. Open **PR** with green CI.

Questions? Create an issue or join discussions!

## 📜 License
**Apache License 2.0** — free for personal & commercial use.

> Built with ☕ & ❤️ by [@AdmiralXy](https://github.com/AdmiralXy) — Happy coding!
