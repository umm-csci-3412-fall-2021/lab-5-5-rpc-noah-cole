# Remote procedure call (RPC) for Currency Exchange <!-- omit in toc -->

[![Tests](../../actions/workflows/gradle_tests.yml/badge.svg)](../../actions/workflows/gradle_tests.yml)

- [Overview](#overview)
- [RPC encapsulation](#rpc-encapsulation)
  - [Reading from a URL](#reading-from-a-url)
  - [Parsing JSON](#parsing-json)
- [Building and running this project](#building-and-running-this-project)
  - [The Gradle build tool](#the-gradle-build-tool)
  - [Using environment variables to store access keys](#using-environment-variables-to-store-access-keys)
  - [How do you set an environment variable?](#how-do-you-set-an-environment-variable)
  - [Environment variables and GithubActions](#environment-variables-and-githubactions)
- [What to turn in](#what-to-turn-in)

## Overview

This lab explores the idea of client-server organization.  In a client-server
configuration one central machine called the **server** acts as a central source
for some resource or service.  Other machines known as **clients** utilize the
resource or service provided by the server.  A good example would be a web-server
providing web-pages to multiple browsers on multiple computers.  

The term **client-server** refers to the configuration of the service provider and
service consumer.  It does not require multiple machines. It is possible for the same
computer to act as server and as client.

This lab illustrates the encapsulation of a
JSON-based web service as a simple remote procedure call (RPC), where we wrap
a call to a remote service (a service which provides currency exchange rate data)
in a way that allows users to access that service through what appear to be
local function calls.

## RPC encapsulation

Financial programs often need access to financial rates such as stock prices and
exchange rates. There are numerous services out there that provide that
data, often through rich (and complex) APIs. There are also, however,
simple services that provide the data in response to
standard HTTP requests.

[Fixer.io](http://fixer.io/), for
example, provides a simple currency exchange rate service that allows
you to specify a date as components of a URL, and generates the exchange
rates for a number of major currency for that date. If you add a working access key (which requires free registration), for example, the URL
<http://data.fixer.io/api/2008-10-15?access_key=...>
generates an JSON document containing a variety of exchange rates for the date specified in the URL (15 Oct 2008). See [Fixer's
documentation](https://fixer.io/documentation)
for more info.

:bangbang: Managing access keys (which should inherently be private) is somewhat
tricky with public repositories like we have here on GitHub.com. There's more below
on how we're going to handle this.

This is nice if we just want to look up a single date and
read through it by hand, but is somewhat awkward if we want to access
this data programmatically (i.e., as part of a piece of software we're
writing). The goal of this lab is to build a simple remote procedure call (RPC)
encapsulation of this service, essentially providing a wrapper that
isolates users (programmers in this case) from the details of accessing
and parsing the data. In this lab you'll provide implementations for two key methods:

```java
public float getExchangeRate(String currencyCode, int year, int month, int day);

public float getExchangeRate(String fromCurrency, String toCurrency, int year, int month, int day);
```

The first provides the exchange on the given date for the given currency
against the base currency (which for Fixer is the Euro). The second
takes a date and two currencies, and returns the exchange rate of the
first vs. the second. Currencies are specified using [ISO 4217 currency
codes](http://en.wikipedia.org/wiki/ISO_4217), and dates are the year
(as a four digit integer), the month as a two digit integer (01=Jan,
12=Dec), and the day of the month as a two digit integer.

We've provided stubs for both of these in the project in the `ExchangeRateReader`
class. Our stubs both throw `UnsupportedOperationException`s; this is a way of
telling Java that you haven't (yet) implemented a method. By throwing them here
we're ensuring that an exception will be thrown (and all dependent tests will
fail) until you've actually implemented the methods. Once you have one of them
implemented, you should remove the `throws` line or your tests will continue to
fail even if you've got everything working.

We've also provided
some simple JUnit tests in the project in `ExchangeRateTest`. The
first four tests all reference static JSON files provided on
`facultypages.morris.umn.edu`; these are also included in the project in the
`JSON_files` directory. The fifth one (which is initially marked with
`@Ignore` so it won't actually run) refers to Fixer's web site. You should
wait until you get the first four to pass before you try the last one as
we don't want to be hammering on Fixer's web site while we're trying to
get our code to work. When you're ready to run that last test just
remove the `@Ignore` line, add a working access key, and it will run.

There are two major pieces here that you may have never seen:

- You'll need to read the result of requesting a URL
- You'll need to parse an JSON document

### Reading from a URL

This is actually quite easy in Java. This little block of code:

```java
String urlString = "http://www.morris.umn.edu/";
URL url = new URL(urlString);
InputStream inputStream = url.openStream();
```

will generate an `InputStream` that will provide the (HTML) contents of
the University of Minnesota Morris home page. You can then pass that `InputStream` to any other reading
tools like a `BufferedReader` or (or more importantly for this lab) an
JSON parser.

In this problem, you'll need to
make sure to construct a full URL, with the relevant query information and
(especially when talking directly to Fixer.io) including the API key. There's
an example of what this looks like up above, and there are more details and
examples in [the Fixer.io documentation](https://fixer.io/documentation).

### Parsing JSON

There are a ton of Java JSON parsing tools out there, including several
included as part of Java's standard libraries. ["How to Parse JSON in Java?"](https://coderolls.com/parse-json-in-java/)
does a nice job of reviewing several of the more popular ones, including
simple examples of each. We used the
[JSON-java](https://github.com/stleary/JSON-java)
library, and the following discussion will be based on that, but you can certainly
use a different library if you prefer.

The basic structure of our solution is:

- Construct a `JSONTokener` using the `InputStream` you get from `URL`
  (as described above).
- Construct a `JSONObject` using the `JSONTokener` you just built.
- Once you have a `JSONObject` you can use method calls like
  `getJSONObject("rates")` and `getFloat(currencyCode)` to extract the
  necessary elements from the returned JSON.

You might want to write a method `getRateForCurrency(JsonObject ratesInfo, String currency)` that encapsulates the walking through the JSON object so you don't end up repeating that logic in your solution.

## Building and running this project

### The Gradle build tool

This project is set up to use the `gradle` build tool to compile the project
and run the tests. The `gradle` configuration is in the `build.gradle` file;
you can ignore more of this but there are a few bits where it might be useful
or necessary to make changes there. In particular `gradle` is responsible for
managing dependencies on external libraries like JSON-Java. We have that
dependency already listed in the `dependencies` section of `build.gradle`,
but if you choose to use a different library you'll need to add (and commit)
that dependency.

There is a `main()` which will prompt you for a currency code and return the
exchange rate for that currency. To run that:

```text
   ./gradlew --console=plain --quiet run
```

:information_source: The use of `gradlew` instead of `gradle` may seem
confusing. `gradlew` is a _wrapper_ (hence the `w`) script that actually
downloads and installs (in your project's `.gradle` directory) the appropriate
version of `gradle` and runs that local version. This ensures consistency
across machines and setups.

You don't strictly need `--console=plain --quiet`, but including them will
reduce the amount of noise that `gradle` outputs.

To run the tests:

```text
    ./gradlew test
```

Both of these will ensure that all dependencies are downloaded and everything
is compiled and up-to-date before running the program or tests.

:bangbang: All of this will fail until you've set the `FIXER_IO_ACCESS_KEY`
environment variable as described below. If all your tests fail check to see
if the `MissingAccessKeyException` is being thrown. If it is, then that's
your problem.

### Using environment variables to store access keys

There's a tricky question here about how to handle access keys. Some
options include:

- Putting the access key directly in the code
  - You could, e.g., define a string constant that is the API key and concatenate
    it into URL strings as needed.
- Put the access key in a properties or configuration file
  - You could then read it from that file, and use it to build URL strings
    as needed.
- Store the access key from an environment variable
  - You could then read it from the environment, and use it to build URL strings
    as needed.

The first two of these have a challenge because in both cases there's a natural
tendency (and thus a risk) that someone will _commit_ that code and your API key
will be publicly visible on GitHub.com.

You can partially deal with this by using `.gitignore` to indicate that a particular
source or properties/configuration file should never be committed. You really need
to isolate the access key in a single (small) file, though, for this to work. In the
first approach, for example, if the access key is defined in a file that has a ton
of other important logic in it then `.gitignore`ing it will prevent a lot of other
code from being committed, which will be a significant problem.

Both the first and second approaches have serious issues with a continuous
integration (CI) system like GitHub Actions. CI systems typically clones the
repository and expect it to build "as is". If you've used `.gitignore` to prevent
an important file from being committed, then the code the CI gets in a clone
either won't compile at all, or will compile but fail at runtime when the
necessary properties/configuration file turns out to be missing.

So the "recommended" approach these days is to use environment variables, and
that's how this code is set up, as illustrated in the `readAccessKey()` method
in `ExchangeRateReader.java`:

```java
    private void readAccessKey() {
        // Read the desired environment variable.
        accessKey = System.getenv("FIXER_IO_ACCESS_KEY");
        // If that environment variable isn't defined, then
        // `getenv()` returns `null`. We'll throw a (custom)
        // exception if that happens since the program can't
        // really run if we don't have an access key.
        if (accessKey == null) {
            throw new MissingAccessKeyException();
        }
    }
```

Here this uses `System.getenv()` to read the value of the specified
environment variable, `"FIXER_IO_ACCESS_KEY"` in this case. Each user will then
need to define that variable in their development environment for this code to
actually run; if they don't the code will throw a `MissingAccessKeyException`.

### How do you set an environment variable?

How you set an environment variable differs depending on your operating system
and shell. For `bash`-based systems (the lab computers and most MacOS systems)
a command like:

```bash
   export FIXER_IO_ACCESS_KEY="frogs-are-green"
```

will set the variable (`FIXER_IO_ACCESS_KEY`) to have the specified value
(`"frogs-are-green"` in this case). :warning: Unlike most computing languages,
you can _not_ have white space on either side of the `=` in an assignment like
that.

The `export` is necessary to make sure this assignment is "exported" to any child
processes/shells created from this shell. That's necessary so that the assignment
will be visible in processes or shells created by or run from this shell; without
it those child processes won't know anything about this variable assignment. Since
we need this to be visible when we run the code with `gradle`, it's vital that we
include the `export`. (See posts like ["Defining a Bash Variable with or without `export`"](https://www.baeldung.com/linux/bash-variables-export) for more details.)

You _could_ add this to something like your `.bashrc` or `.bash_profile` so it will
be automatically assigned in every shell you create, but then you'd need to make
that file only readable by you to protect the API key from snoopy people. Since
you'll only need this for a brief period (a week-ish), then it's probably easiest
to just re-define it at each work session so you don't have to remember to remove
it from whatever setup you create.

### Environment variables and GithubActions

One of the big reasons we're using environment variables instead of the other two
options (putting the key in code or in a properties file) is that this allows us
to run our tests in GitHub Actions without committing the secret info (in this
case, the Fixer.io access key). Like most other continuous integration systems
these days, GitHub provides support for secret environment variables, where
our secrets are held in an encrypted form and then provided as environment
variables to the build process in GitHub Actions; see [the GitHub Encrypted
Secrets documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
for more details.

GitHub's tools support including the secret at the level of the repository,
a user-level environment, or an organization. If you're doing this lab as part
of a course using GitHub Classroom, the instructor has probably added the
key to the classroom organization so your GitHub Actions builds should "just
work" for you. If they don't, and the problem seems to be an missing access
key contact the instructor for help; in a pinch you could add the key as an
encrypted secret at the repository level.

## What to turn in

You should complete the code in `ExchangeRateReader.java` so that

- [ ] The tests pass locally (`/.gradlew test`)
- [ ] The tests pass on GitHub (your badge should turn green)
- [ ] The code is clean and clear.

Also make sure to submit a link to your repository.
