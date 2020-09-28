/* eslint max-len: 0 */
import React, { useContext } from 'react';
import { Accordion, Card } from 'react-bootstrap';
import AccordionContext from 'react-bootstrap/AccordionContext';
import { useAccordionToggle } from 'react-bootstrap/AccordionToggle';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import routes from '../../routes';

function Help() {
    function ContextAwareToggle({ children, eventKey, callback }) {
        const currentEventKey = useContext(AccordionContext);
        const decoratedOnClick = useAccordionToggle(
            eventKey,
            () => callback && callback(eventKey),
        );
        const isCurrentEventKey = currentEventKey === eventKey;
        return (
            <h5>
                <a onClick={decoratedOnClick} href={`#accordion-${eventKey}`}>
                    {children}
                    <i className={`mdi ${isCurrentEventKey ? 'mdi-minus' : 'mdi-plus'}`} />
                </a>
            </h5>
        );
    }

    ContextAwareToggle.propTypes = {
        // eslint-disable-next-line react/forbid-prop-types
        children: PropTypes.object.isRequired,
        eventKey: PropTypes.string.isRequired,
        callback: PropTypes.func.isRequired,
    };

    return (
        <>
            <div className="row">
                <div className="col-md-12 grid-margin">
                    <h2>How do I use the AEM Component Generator?</h2>
                </div>
            </div>
            <div className="row">
                <div className="col-12 col-md-6">
                    <div className="col-12 grid-margin">
                        <div className="card">
                            <div className="faq-block card-body">
                                <div className="container-fluid py-2">
                                    <h5 className="mb-0">
                                        What are the
                                        {' '}
                                        <Link to={routes.config}>Global Config</Link>
                                        {' '}
                                        properties?
                                    </h5>
                                </div>
                                <Card>
                                    <Card.Header>
                                        Options
                                    </Card.Header>
                                    <Card.Body name="accordion-0">
                                        <ul>
                                            <li>
                                                <strong>Code owner: </strong>
                                                <span>
                                                    The name of the company/user this code base belongs to - will replace
                                                    {' ${'}
                                                    CODEOWNER&#125; in the template files with this configured value
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Bundle Path: </strong>
                                                <span>
                                                    The relative path to the java code of your main bundle. e.g. core/src/main/java
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Test Path </strong>
                                                <span>
                                                    The relative path to the java code of your test cases. E.g. core/src/test/java
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Apps Path </strong>
                                                <span>
                                                    The relative path to the /apps root of your project: e.g. ui.apps/src/main/content/jcr_root/apps
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Component Path </strong>
                                                <span>
                                                    The path to the project&apos;s components directory, relative to the /apps folder
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Model interface package </strong>
                                                <span>
                                                    The Java package for the interface model objects
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Model implementation package </strong>
                                                <span>
                                                    Java package for the implementation model objects
                                                </span>
                                            </li>
                                            <li>
                                                <strong>Copyright year </strong>
                                                <span>
                                                    The year you want set for the generated copyright text
                                                </span>
                                            </li>
                                        </ul>
                                    </Card.Body>
                                </Card>
                            </div>
                        </div>
                    </div>
                    <div className="col-12 grid-margin grid-margin-md-0">
                        <div className="card">
                            <div className="faq-block card-body">
                                <div className="container-fluid py-2">
                                    <h5 className="mb-0">
                                        What are the component
                                        {' '}
                                        <Link to={routes.dialogProperties}>Dialog Properties?</Link>
                                        {' '}
                                    </h5>
                                </div>
                                <Accordion defaultActiveKey="9">
                                    <Card>
                                        <Card.Header>
                                            <ContextAwareToggle eventKey="9">
                                                Main properties
                                            </ContextAwareToggle>
                                        </Card.Header>
                                        <Accordion.Collapse eventKey="9">
                                            <Card.Body name="accordion-9">
                                                <ul>
                                                    <li>
                                                        <strong>Field node name: </strong>
                                                        <span>the property node name and java variable name.</span>
                                                    </li>
                                                </ul>
                                            </Card.Body>
                                        </Accordion.Collapse>
                                    </Card>
                                    <Card>
                                        <Card.Header>
                                            <ContextAwareToggle eventKey="10">Shared / Global</ContextAwareToggle>
                                        </Card.Header>
                                        <Accordion.Collapse eventKey="10">
                                            <Card.Body name="accordion-10">
                                                Define properties here to create a shared dialog for this component. If empty, no shared dialog will be created.
                                                <br />
                                                See&nbsp;
                                                <a href="https://adobe-consulting-services.github.io/acs-aem-commons/features/shared-component-properties/index.html">Shared Component Properties</a>
                                                &nbsp;for more information on how shared dialog properties work.
                                            </Card.Body>
                                        </Accordion.Collapse>
                                    </Card>
                                </Accordion>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="col-12 col-md-6">
                    <div className="col-12 grid-margin">
                        <div className="card">
                            <div className="faq-block card-body">
                                <div className="container-fluid py-2">
                                    <h5 className="mb-0">
                                        What are the
                                        {' '}
                                        <Link to={routes.compConfig}>Component Config</Link>
                                        {' '}
                                        properties?
                                    </h5>
                                </div>
                                <Accordion defaultActiveKey="7">
                                    <Card>
                                        <Card.Header>
                                            <ContextAwareToggle eventKey="7">
                                                Component Attributes
                                            </ContextAwareToggle>
                                        </Card.Header>
                                        <Accordion.Collapse eventKey="7">
                                            <Card.Body name="accordion-7">
                                                <ul>
                                                    <li>
                                                        <strong>Component Title: </strong>
                                                        <span>the human readable component name, also used as the title for dialogs</span>
                                                    </li>
                                                    <li>
                                                        <strong>Component Node name: </strong>
                                                        <span>the folder name for the component (will be saved to AEM as a sling folder)</span>
                                                    </li>
                                                    <li>
                                                        <strong>Component Group: </strong>
                                                        <span>
                                                            Group under which the component can be selected in the Components browser (touch-enabled UI) or Sidekick (classic UI).
                                                            A value of .hidden is used for components that are not available for selection from the UI such as the actual paragraph systems.
                                                        </span>
                                                    </li>
                                                    <li>
                                                        <strong>Component Type: </strong>
                                                        <span>component folder type - content, form, or structure</span>
                                                    </li>
                                                    <li>
                                                        <strong>Model Adapters: </strong>
                                                        <span>The adaptables to include in the Sling Model (&quot;request&quot; and/or &quot;resource&quot;)</span>
                                                    </li>
                                                </ul>
                                            </Card.Body>
                                        </Accordion.Collapse>
                                    </Card>
                                    <Card>
                                        <Card.Header>
                                            <ContextAwareToggle eventKey="8">Additional Options</ContextAwareToggle>
                                        </Card.Header>
                                        <Accordion.Collapse eventKey="8">
                                            <Card.Body name="accordion-8">
                                                <ul>
                                                    <li>
                                                        <strong>Add js: </strong>
                                                        <span>whether to create an empty JS client library for the component (shared with CSS lib)</span>
                                                    </li>
                                                    <li>
                                                        <strong>Add JS text file?: </strong>
                                                        <span>whether to create the js.txt mapping file within the clientlib. Set to false when this file is not needed within your clientlib</span>
                                                    </li>
                                                    <li>
                                                        <strong>Add css?: </strong>
                                                        <span>
                                                            whether to create an empty CSS client library for the component (shared with JS lib)
                                                        </span>
                                                    </li>
                                                    <li>
                                                        <strong>Add CSS text file?: </strong>
                                                        <span>whether to create the css.txt mapping file within the clientlib. Set to false when this file is not needed within your clientlib</span>
                                                    </li>
                                                    <li>
                                                        <strong>Add HTML file?: </strong>
                                                        <span>whether to create a default HTML file for the component</span>
                                                    </li>
                                                    <li>
                                                        <strong>include stubbed HTML content?: </strong>
                                                        <span>whether to create a sample HTML content using all the dialog properties</span>
                                                    </li>
                                                    <li>
                                                        <strong>create Sling model?: </strong>
                                                        <span>
                                                            whether to create a backing sling model for the component
                                                            <br />
                                                            The Class name is derived from converting &quot;component node name&quot; covered above to camel case
                                                            <br />
                                                            (e.g. &quot;google-maps&quot; -&gt; GoogleMaps/GoogleMapsImpl)
                                                        </span>
                                                    </li>
                                                    <li>
                                                        <strong>create test class?: </strong>
                                                        <span>
                                                            whether to create a stubbed test class for the component&apos;s sling model
                                                            <br />
                                                            The test methods will fail with reason as yet to be implemented.
                                                        </span>
                                                    </li>
                                                    <li>
                                                        <strong>add generic java docs?: </strong>
                                                        <span>whether to create generic javadoc for the getters in the model interface</span>
                                                    </li>
                                                    <li>
                                                        <strong>add content exporter annotation?: </strong>
                                                        <span>Whether to configure sling model for content export</span>
                                                    </li>
                                                    <li>
                                                        <strong>junit version?: </strong>
                                                        <span>provide major version identifier of junit to generate test classes accordingly. Currently, 4 or 5 is supported.</span>
                                                    </li>
                                                </ul>
                                            </Card.Body>
                                        </Accordion.Collapse>
                                    </Card>
                                </Accordion>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default Help;
